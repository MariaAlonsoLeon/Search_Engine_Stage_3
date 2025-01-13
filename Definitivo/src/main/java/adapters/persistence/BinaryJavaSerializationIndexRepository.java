package adapters.persistence;

import domain.IndexRepository;
import domain.InvertedIndex;

import java.io.*;

public class BinaryJavaSerializationIndexRepository implements IndexRepository {
    private final String filePath;
    private InvertedIndex cachedIndex;

    public BinaryJavaSerializationIndexRepository(String filePath) {
        this.filePath = filePath;
        initializeEmptyIndex();
    }

    @Override
    public synchronized void saveInvertedIndex(InvertedIndex newIndex) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(newIndex);
            cachedIndex = newIndex;
            System.out.println("Inverted index successfully saved to Binary file: " + filePath);
        } catch (IOException e) {
            System.out.println("Error saving inverted index to file: " + filePath);
            throw new RuntimeException("Error saving inverted index", e);
        }
    }

    @Override
    public synchronized InvertedIndex findInvertedIndex() {
        if (cachedIndex != null) {
            System.out.println("Inverted index loaded from cache.");
            return cachedIndex;
        }

        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            System.out.println("Inverted index file is missing or empty: " + filePath);
            return new InvertedIndex();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            cachedIndex = (InvertedIndex) ois.readObject();
            System.out.println("Inverted index loaded successfully from " + filePath);
            return cachedIndex;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading inverted index from file: " + filePath);
            throw new RuntimeException("Error loading inverted index", e);
        }
    }

    private void initializeEmptyIndex() {
        File file = new File(filePath);
        if (!file.exists()) {
            saveInvertedIndex(new InvertedIndex());
        }
    }
}
