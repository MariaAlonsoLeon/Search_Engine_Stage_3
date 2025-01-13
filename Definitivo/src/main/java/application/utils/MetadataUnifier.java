package application.utils;

import com.hazelcast.collection.IList;
import domain.Metadata;
import domain.MetadataRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetadataUnifier {

    private final IList<Metadata> metadata;
    private final MetadataRepository persistentMetadataRepository;

    public MetadataUnifier(IList<Metadata> metadata, MetadataRepository persistentMetadataRepository) {
        this.metadata = metadata;
        this.persistentMetadataRepository = persistentMetadataRepository;
    }

    public List<Metadata> unifyMetadata() {
        Set<Metadata> metadataList = new HashSet<>();
        List<Metadata> fileMetadata = persistentMetadataRepository.findAllMetadata();
        for (Metadata metadata1 : metadata) {
            metadataList.add(metadata1);
        }
        for (Metadata metadata2 : fileMetadata) {
            metadataList.add(metadata2);
        }

        return metadataList.stream().toList();
    }
}
