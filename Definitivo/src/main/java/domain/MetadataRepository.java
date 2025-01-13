package domain;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository {
    void saveMetadata(Metadata metadata);

    List<Metadata> findAllMetadata();
}