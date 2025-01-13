package adapters.persistence;

import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import domain.Metadata;
import domain.MetadataRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HazelcastMetadataRepository implements MetadataRepository {
    private final IList<Metadata> hazelcastMetadataList;

    public HazelcastMetadataRepository(HazelcastInstance hazelcastInstance) {
        this.hazelcastMetadataList = hazelcastInstance.getList("metadata");
    }

    @Override
    public void saveMetadata(Metadata metadata) {
        hazelcastMetadataList.add(metadata);
    }

    @Override
    public List<Metadata> findAllMetadata() {
        return new ArrayList<>(hazelcastMetadataList);
    }
}
