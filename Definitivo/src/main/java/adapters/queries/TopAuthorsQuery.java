package adapters.queries;

import com.hazelcast.collection.IList;
import domain.Metadata;
import domain.MetadataRepository;
import domain.Query;
import domain.QueryResult;
import application.utils.MetadataUnifier;

import java.util.*;
import java.util.stream.Collectors;

public class TopAuthorsQuery implements Query {
    private final IList<Metadata> metadata;
    private final MetadataRepository persistentMetadataRepository;
    private final MetadataUnifier metadataUnifier;

    public TopAuthorsQuery(IList<Metadata> metadata, MetadataRepository persistentMetadataRepository) {
        this.metadata = metadata;
        this.persistentMetadataRepository = persistentMetadataRepository;
        this.metadataUnifier = new MetadataUnifier(metadata, persistentMetadataRepository);
    }

    @Override
    public QueryResult execute() {
        List<Metadata> unifiedMetadata = metadataUnifier.unifyMetadata();

        Map<String, Long> authorCounts = unifiedMetadata.stream()
                .map(Metadata::getAuthor)
                .filter(author -> author != null && !author.equalsIgnoreCase("Not found"))
                .collect(Collectors.groupingBy(author -> author, Collectors.counting()));

        List<String> results = authorCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(entry -> String.format("{author: %s, document_count: %d}", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return new QueryResult(results, results.size(), "author_distribution");
    }
}

