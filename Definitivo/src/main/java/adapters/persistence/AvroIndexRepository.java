package adapters.persistence;

import domain.IndexRepository;
import domain.InvertedIndex;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroIndexRepository implements IndexRepository {

    private final String filePath;
    private final Schema schema;

    public AvroIndexRepository(String filePath) throws IOException {
        this.filePath = filePath;
        String schemaResourcePath = "InvertedIndex.avsc";

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
    public void saveInvertedIndex(InvertedIndex invertedIndex) {
        File file = new File(filePath);

        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(schema))) {
            dataFileWriter.create(schema, file);

            for (Map.Entry<String, Map<String, InvertedIndex.WordData>> wordEntry : invertedIndex.getIndex().entrySet()) {
                GenericRecord record = new GenericData.Record(schema);
                record.put("word", wordEntry.getKey());

                List<GenericRecord> books = new ArrayList<>();
                for (Map.Entry<String, InvertedIndex.WordData> bookEntry : wordEntry.getValue().entrySet()) {
                    GenericRecord bookRecord = new GenericData.Record(schema.getField("books").schema().getElementType());
                    bookRecord.put("name", bookEntry.getKey());
                    bookRecord.put("positions", bookEntry.getValue().getPositions());
                    bookRecord.put("frequency", bookEntry.getValue().getFrequency());
                    books.add(bookRecord);
                }

                record.put("books", books);
                dataFileWriter.append(record);
            }

            System.out.println("Inverted index successfully saved to Avro file: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to Avro file: " + e.getMessage());
        }
    }

    @Override
    public InvertedIndex findInvertedIndex() {
        InvertedIndex invertedIndex = new InvertedIndex();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return invertedIndex;
        }

        try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(file, new GenericDatumReader<>(schema))) {
            while (dataFileReader.hasNext()) {
                GenericRecord record = dataFileReader.next();
                String word = record.get("word").toString();

                List<GenericRecord> books = (List<GenericRecord>) record.get("books");
                Map<String, InvertedIndex.WordData> bookDataMap = new HashMap<>();
                for (GenericRecord book : books) {
                    String name = book.get("name").toString();
                    List<Integer> positions = (List<Integer>) book.get("positions");
                    int frequency = (int) book.get("frequency");

                    InvertedIndex.WordData wordData = new InvertedIndex.WordData();
                    wordData.setPositions(positions);
                    wordData.setFrequency(frequency);
                    bookDataMap.put(name, wordData);
                }

                invertedIndex.getIndex().put(word, bookDataMap);
            }

            System.out.println("Inverted index successfully loaded from Avro file.");
        } catch (IOException e) {
            System.err.println("");
        }

        return invertedIndex;
    }
}
