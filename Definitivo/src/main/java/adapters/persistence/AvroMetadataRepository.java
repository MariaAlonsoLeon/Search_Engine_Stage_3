package adapters.persistence;

import domain.Metadata;
import domain.MetadataRepository;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AvroMetadataRepository implements MetadataRepository {

    private final Schema schema;
    private final String filePath;

    public AvroMetadataRepository(String filePath) throws IOException {
        this.filePath = filePath;
        String schemaResourcePath = "Metadata.avsc";

        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(schemaResourcePath)) {
            if (schemaStream == null) {
                throw new IOException("Schema file not found in resources: " + schemaResourcePath);
            }
            Schema.Parser parser = new Schema.Parser();
            this.schema = parser.parse(schemaStream);
        } catch (IOException e) {
            throw new IOException("Error reading Avro schema file from resources: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveMetadata(Metadata metadata) {
        Map<String, Metadata> metadataMap = loadAllMetadataAsMap();

        metadataMap.put(metadata.getBookId(), metadata);

        File file = new File(filePath);

        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(schema))) {
            dataFileWriter.create(schema, file);

            for (Metadata meta : metadataMap.values()) {
                GenericRecord record = new GenericData.Record(schema);
                record.put("bookId", meta.getBookId());
                record.put("author", meta.getAuthor());
                record.put("date", meta.getDate());
                record.put("language", meta.getLanguage());
                dataFileWriter.append(record);
            }

            System.out.println("Metadata successfully saved to Avro file: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to Avro file: " + e.getMessage());
        }
    }

    @Override
    public List<Metadata> findAllMetadata() {
        return new ArrayList<>(loadAllMetadataAsMap().values());
    }

    private Map<String, Metadata> loadAllMetadataAsMap() {
        Map<String, Metadata> metadataMap = new HashMap<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Metadata file not found: " + filePath);
            return metadataMap;
        }

        try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(file, new GenericDatumReader<>(schema))) {
            while (dataFileReader.hasNext()) {
                GenericRecord record = dataFileReader.next();
                Metadata metadata = new Metadata(
                        record.get("bookId").toString(),
                        record.get("author").toString(),
                        record.get("date").toString(),
                        record.get("language").toString()
                );
                metadataMap.put(metadata.getBookId(), metadata);
            }

            System.out.println("Metadata successfully loaded from Avro file.");
        } catch (IOException e) {
            System.err.println("");
        }

        return metadataMap;
    }
}
