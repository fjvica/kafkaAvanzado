# Kafka + Spring Boot con Confluent Platform

## Introducción

Este proyecto describe cómo levantar un entorno Kafka local utilizando Docker con Confluent Platform y cómo interactuar con él desde una aplicación Spring Boot. La guía incluye producción y consumo de mensajes, manejo de errores, uso de Avro para serialización y colas de mensajes fallidos (Dead Letter Queue, DLT).

El enfoque está en la explicación de conceptos, arquitectura y configuración de forma narrativa, sin diagramas, para que se comprenda el funcionamiento completo del flujo de mensajes.

---

## 1. Levantando el entorno Kafka

Para trabajar con Kafka en desarrollo, se utilizan los siguientes servicios:

- **Zookeeper**: Es el coordinador del cluster de Kafka. Mantiene el estado del cluster, gestiona la información de los brokers y supervisa la replicación de particiones.
- **Kafka Broker**: Nodo principal que almacena y distribuye los mensajes dentro de topics y particiones. Cada broker puede gestionar múltiples topics y trabajar en conjunto con otros brokers para la replicación y tolerancia a fallos.
- **Schema Registry**: Servicio que almacena y gestiona esquemas de mensajes (Avro, Protobuf, JSON), asegurando compatibilidad entre productores y consumidores. Esto permite que las aplicaciones lean y escriban datos estructurados sin riesgo de incompatibilidades.
- **Control Center**: Interfaz web que permite monitorear topics, consumidores, offsets, lag, conectores y la salud general del cluster de Kafka. También permite visualizar métricas y estados de replicación.

**Claves de la configuración en Docker Compose**:

- `KAFKA_ADVERTISED_LISTENERS`: Define cómo se puede acceder al broker desde dentro del cluster y desde el host local.
- `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR`: Ajustado a 1 en desarrollo para evitar errores de creación de topics internos con un único broker.
- `SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS`: Permite al Schema Registry conectarse al broker para almacenar y recuperar esquemas.

Con esta configuración, los topics se crean automáticamente, los productores pueden enviar mensajes y los consumidores leerlos sin problemas.

---

## 2. Topics y flujo de mensajes

Un **topic** es la unidad lógica donde se almacenan los mensajes. Puede dividirse en varias particiones, lo que permite paralelismo y escalabilidad.

Los mensajes enviados por los productores se distribuyen entre las particiones, y cada mensaje tiene un **offset**, que indica su posición dentro de la partición. Los consumidores usan los offsets para rastrear qué mensajes han procesado y qué mensajes están pendientes.

Para probar el flujo de mensajes, se crean topics de prueba y se configuran consumidores y productores desde la aplicación Spring Boot.

---

## 3. Conceptos fundamentales de Kafka

Durante el proyecto se han trabajado los siguientes conceptos:

- **Broker**: Nodo que almacena particiones de topics y sirve mensajes a consumidores.
- **Topic**: Cola lógica de mensajes que puede tener múltiples particiones.
- **Partition**: Subdivisión de un topic; permite escalabilidad y paralelismo.
- **Producer**: Componente que envía mensajes a un topic.
- **Consumer**: Componente que recibe mensajes de un topic.
- **Consumer Group**: Conjunto de consumidores que reparten el consumo de particiones de un topic entre ellos.
- **Offset**: Posición de un mensaje dentro de una partición; se usa para tracking del consumo.
- **Replication**: Copia de particiones en varios brokers para tolerancia a fallos.
- **ISR (In-Sync Replica)**: Réplicas actualizadas y sincronizadas con la partición líder.
- **Delivery Semantics**: Garantías de entrega de mensajes:
    - `at-most-once`: Se pueden perder mensajes, pero no se duplican.
    - `at-least-once`: Se entregan al menos una vez, posibles duplicados.
    - `exactly-once`: Cada mensaje se procesa exactamente una vez.
- **Retention Policy**: Tiempo que Kafka mantiene los mensajes antes de eliminarlos automáticamente.
- **Log Compaction**: Mantiene solo el último mensaje por key, útil para snapshots de estado.

---

## 4. Spring Boot y Kafka

Se configuró una aplicación Spring Boot para producir y consumir mensajes:

- **KafkaTemplate**: Clase principal para enviar mensajes a topics de forma síncrona o asíncrona.
- **KafkaListener**: Anotación para definir métodos consumidores que escuchan topics específicos.
- **Serializers/Deserializers**: Se usaron Avro serializers para convertir objetos Java a bytes y viceversa. Esto requiere indicar la URL del Schema Registry.
- **Callbacks y errores**: Los envíos asíncronos pueden registrar callbacks de éxito o fallo para monitorear la entrega de mensajes.

Con estas configuraciones, la aplicación produce 100 mensajes al iniciar y los consumidores los procesan inmediatamente.

---

## 5. Manejo de errores y Dead Letter Queue (DLT)

Para evitar que mensajes erróneos bloqueen el flujo:

- Se configuró un **DefaultErrorHandler** en Spring Kafka.
- Los mensajes que no se pueden procesar se redirigen a un topic de DLT (`test-topic-dlt`).
- Esto permite revisar los mensajes fallidos y tomar decisiones de reprocesamiento sin afectar el flujo normal de los consumers.

El consumer de la DLT debe usar el mismo deserializer que el topic original para poder interpretar correctamente los mensajes.

---

## 6. Glosario de conceptos clave

| Concepto | Definición |
|----------|------------|
| Broker | Nodo de Kafka que almacena particiones de topics y sirve mensajes a consumidores. |
| Topic | Cola lógica de mensajes; puede subdividirse en particiones. |
| Partition | División de un topic; permite paralelismo y replicación. |
| Producer | Componente que envía mensajes a un topic. |
| Consumer | Componente que recibe mensajes de un topic. |
| Consumer Group | Conjunto de consumidores que comparten el consumo de particiones de un topic. |
| Offset | Posición de un mensaje dentro de una partición. |
| Replication | Copia de particiones en varios brokers para tolerancia a fallos. |
| ISR | Réplicas sincronizadas con la partición líder. |
| Delivery Semantics | Garantías de entrega de mensajes (at-most-once, at-least-once, exactly-once). |
| Retention Policy | Tiempo que Kafka conserva mensajes antes de eliminarlos. |
| Log Compaction | Conserva solo el último mensaje por key para mantener snapshots de estado. |
| Schema Registry | Servicio que gestiona esquemas de mensajes para compatibilidad entre productores y consumidores. |
| Dead Letter Queue (DLT) | Topic donde se envían los mensajes que fallan al ser consumidos. |