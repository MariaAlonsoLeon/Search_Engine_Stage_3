package application.utils;

import adapters.persistence.*;
import domain.IndexRepository;
import domain.MetadataRepository;

import java.io.IOException;

public class DataTypeSelector {

    private final IndexRepository indexRepository;
    private final MetadataRepository metadataRepository;
    private final String filePathIndex = "invertedIndex";
    private final String filePathMetadata = "metadata";
    private final String extension;

    public DataTypeSelector(String dataType) throws IOException {
        switch (dataType.toLowerCase()) {
            case "text":
                extension = ".txt";
                this.indexRepository = new FileIndexRepository(filePathIndex + extension);
                this.metadataRepository = new FileMetadataRepository(filePathMetadata + extension);
                break;

            case "cbor":
                extension = ".cbor";
                this.indexRepository = new CBORIndexRepository(filePathIndex + extension);
                this.metadataRepository = new CBORMetadataRepository(filePathMetadata + extension);

                break;
            case "binary":
                extension = ".bin";
                this.indexRepository = new BinaryJavaSerializationIndexRepository(filePathIndex + extension);
                this.metadataRepository = new BinaryJavaSerializationMetadataRepository(filePathMetadata + extension);

                break;
            case "kryo":
                extension = ".kryo";
                this.indexRepository = new KryoIndexRepository(filePathIndex + extension);
                this.metadataRepository = new KryoMetadataRepository(filePathMetadata + extension);
                break;

            case "avro":
                extension = ".avro";
                this.indexRepository = new AvroIndexRepository(filePathIndex + extension);
                this.metadataRepository = new AvroMetadataRepository(filePathMetadata + extension);
                break;

            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }

    public IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public MetadataRepository getMetadataRepository() {
        return metadataRepository;
    }
}