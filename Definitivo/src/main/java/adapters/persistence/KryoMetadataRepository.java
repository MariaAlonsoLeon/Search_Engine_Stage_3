package adapters.persistence;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import domain.Metadata;
import domain.MetadataRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KryoMetadataRepository implements MetadataRepository {
    private final String filePath;
    private final Kryo kryo;

    public KryoMetadataRepository(String filePath) {
        this.filePath = filePath;
        this.kryo = new Kryo();
        this.kryo.register(HashMap.class);
        this.kryo.register(Metadata.class);
    }

    @Override
    public void saveMetadata(Metadata metadata) {
        Map<String, Metadata> metadataMap = loadAllMetadata();
        metadataMap.put(metadata.getBookId(), metadata);
        saveToFile(metadataMap);
    }

    @Override
    public List<Metadata> findAllMetadata() {
        return new ArrayList<>(loadAllMetadata().values());
    }

    private void saveToFile(Map<String, Metadata> metadataMap) {
        try (Output output = new Output(new FileOutputStream(filePath))) {
            kryo.writeObject(output, metadataMap);
            System.out.println("Metadata saved to kryo file");
        } catch (IOException e) {
            System.err.println("Error saving metadata: " + e.getMessage());
        }
    }

    private synchronized Map<String, Metadata> loadAllMetadata() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }

        try (Input input = new Input(new FileInputStream(file))) {
            return kryo.readObject(input, HashMap.class);
        } catch (Exception e) {
            System.err.println("Error loading metadata: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
