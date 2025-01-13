package adapters.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import domain.Metadata;
import domain.MetadataRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CBORMetadataRepository implements MetadataRepository {
    private final File file;
    private final ObjectMapper objectMapper;

    public CBORMetadataRepository(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper(new CBORFactory());
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
        try {
            objectMapper.writeValue(file, metadataMap);
            System.out.println("Metadata successfully saved to CBOR file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Metadata> loadAllMetadata() {
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(file, objectMapper.getTypeFactory()
                    .constructMapType(HashMap.class, String.class, Metadata.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
