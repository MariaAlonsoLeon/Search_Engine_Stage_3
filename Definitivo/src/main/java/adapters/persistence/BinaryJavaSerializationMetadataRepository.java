package adapters.persistence;

import domain.Metadata;
import domain.MetadataRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BinaryJavaSerializationMetadataRepository implements MetadataRepository {
    private final String filePath;
    private List<Metadata> cachedMetadata;

    public BinaryJavaSerializationMetadataRepository(String filePath) {
        this.filePath = filePath;
        initializeEmptyMetadata();
    }

    @Override
    public synchronized void saveMetadata(Metadata metadata) {
        List<Metadata> metadataList = findAllMetadata();
        metadataList.removeIf(existing -> existing.getBookId().equals(metadata.getBookId()));
        metadataList.add(metadata);

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(metadataList);
            cachedMetadata = metadataList;
            System.out.println("Metadata saved successfully to : " + filePath);
        } catch (IOException e) {
            System.out.println("Error saving metadata to file : " + filePath);
            throw new RuntimeException("Error saving metadata", e);
        }
    }

    @Override
    public synchronized List<Metadata> findAllMetadata() {
        if (cachedMetadata != null) {
            System.out.println("Metadata loaded from cache.");
            return cachedMetadata;
        }

        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            System.out.println("Metadata file is missing or empty: " + filePath);
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            cachedMetadata = (List<Metadata>) ois.readObject();
            System.out.println("Metadata loaded successfully from : " + filePath);
            return cachedMetadata;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading metadata from file: " + filePath);
            throw new RuntimeException("Error loading metadata", e);
        }
    }

    private void initializeEmptyMetadata() {
        File file = new File(filePath);
        if (!file.exists()) {
            saveMetadata(new Metadata("", "", "", ""));
        }
    }
}
