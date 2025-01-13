FROM openjdk:22-jdk-slim

WORKDIR /app

COPY target/Definitivo-1.0-SNAPSHOT.jar /app/Definitivo.jar

ENV DATATYPE=avro
ENV INVERTED_INDEX_FILE=invertedIndex
ENV METADATA_FILE=metadata

RUN mkdir -p /app && \
  bash -c 'case "$DATATYPE" in \
    text) touch /app/${INVERTED_INDEX_FILE}.txt /app/${METADATA_FILE}.txt ;; \
    binary) touch /app/${INVERTED_INDEX_FILE}.bin /app/${METADATA_FILE}.bin ;; \
    kryo) touch /app/${INVERTED_INDEX_FILE}.kryo /app/${METADATA_FILE}.kryo ;; \
    cbor) touch /app/${INVERTED_INDEX_FILE}.cbor /app/${METADATA_FILE}.cbor ;; \
    avro) touch /app/${INVERTED_INDEX_FILE}.avro /app/${METADATA_FILE}.avro ;; \
    *) echo "Unsupported data type: $DATATYPE" && exit 1 ;; \
  esac'

COPY src/main/resources /app/static

EXPOSE 4567

CMD ["java", "-jar", "Definitivo.jar"]
