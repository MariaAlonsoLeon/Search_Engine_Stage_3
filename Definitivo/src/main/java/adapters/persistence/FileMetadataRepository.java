package adapters.persistence;

import domain.Metadata;
import domain.MetadataRepository;

import java.io.*;
import java.util.*;

public class FileMetadataRepository implements MetadataRepository {

    private final String filePath;

    public FileMetadataRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void saveMetadata(Metadata metadata) {
        Map<String, Metadata> metadataMap = loadAllMetadata();

        metadataMap.put(metadata.getBookId(), metadata);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, false), "UTF-8"))) {
            for (Metadata meta : metadataMap.values()) {
                writer.write(meta.getBookId() + "," + meta.getAuthor() + "," + meta.getDate() + "," + meta.getLanguage() + "\n");
            }
            System.out.println("Metadata saved to text file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Metadata> findAllMetadata() {
        List<Metadata> metadataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    metadataList.add(new Metadata(parts[0], parts[1], parts[2], parts[3]));
                    for (String part : parts) {
                        System.out.println(part);
                    }
                } else {
                    System.err.println("Skipping malformed metadata entry: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return metadataList;
    }

    private Map<String, Metadata> loadAllMetadata() {
        Map<String, Metadata> metadataMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Metadata metadata = new Metadata(parts[0], parts[1], parts[2], parts[3]);
                    metadataMap.put(parts[0], metadata);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return metadataMap;
    }
}
