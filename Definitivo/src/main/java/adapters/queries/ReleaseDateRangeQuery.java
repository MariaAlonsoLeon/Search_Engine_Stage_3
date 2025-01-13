package adapters.queries;

import com.hazelcast.collection.IList;
import domain.Metadata;
import domain.MetadataRepository;
import domain.Query;
import domain.QueryResult;
import application.utils.MetadataUnifier;

import java.util.*;

public class ReleaseDateRangeQuery implements Query {
    private final IList<Metadata> metadata;
    private final MetadataRepository persistentMetadataRepository;
    private final MetadataUnifier metadataUnifier;

    public ReleaseDateRangeQuery(IList<Metadata> metadata, MetadataRepository persistentMetadataRepository) {
        this.metadata = metadata;
        this.persistentMetadataRepository = persistentMetadataRepository;
        this.metadataUnifier = new MetadataUnifier(metadata, persistentMetadataRepository);
    }

    @Override
    public QueryResult execute() {
        List<Metadata> unifiedMetadata = metadataUnifier.unifyMetadata();

        Optional<String> earliest = unifiedMetadata.stream()
                .map(Metadata::getDate)
                .filter(date -> date != null && !date.equalsIgnoreCase("Not found"))
                .min(String::compareTo);

        Optional<String> latest = unifiedMetadata.stream()
                .map(Metadata::getDate)
                .filter(date -> date != null && !date.equalsIgnoreCase("Not found"))
                .max(String::compareTo);

        String result = String.format("{earliest_date: %s, latest_date: %s}",
                earliest.orElse("N/A"), latest.orElse("N/A"));

        return new QueryResult(Collections.singletonList(result), 1, "release_date_range");
    }

}

