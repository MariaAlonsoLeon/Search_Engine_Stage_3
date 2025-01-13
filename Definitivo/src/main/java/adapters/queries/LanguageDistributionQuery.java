package adapters.queries;

import com.hazelcast.collection.IList;
import domain.Metadata;
import domain.MetadataRepository;
import domain.Query;
import domain.QueryResult;
import application.utils.MetadataUnifier;

import java.util.*;
import java.util.stream.Collectors;

public class LanguageDistributionQuery implements Query {

    private final IList<Metadata> hazelcastMetadataList;
    private final MetadataRepository persistentMetadataRepository;
    private final MetadataUnifier metadataUnifier;

    public LanguageDistributionQuery(IList<Metadata> hazelcastMetadataList, MetadataRepository persistentMetadataRepository) {
        this.hazelcastMetadataList = hazelcastMetadataList;
        this.persistentMetadataRepository = persistentMetadataRepository;
        this.metadataUnifier = new MetadataUnifier(hazelcastMetadataList, persistentMetadataRepository);
    }

    @Override
    public QueryResult execute() {
        List<Metadata> unifiedMetadata = metadataUnifier.unifyMetadata();

        Map<String, Long> languageCounts = unifiedMetadata.stream()
                .collect(Collectors.groupingBy(Metadata::getLanguage, Collectors.counting()));

        List<String> result = languageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());

        return new QueryResult(result, result.size(), "LanguageDistributionQuery");
    }
}
