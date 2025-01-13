package application.services;

import com.hazelcast.core.HazelcastInstance;
import domain.IndexRepository;
import domain.MetadataRepository;
import domain.QueryResult;
import domain.QueryService;

import java.util.List;

public class SearchService {

    private final QueryService queryService;
    private HazelcastInstance hazelcastInstance;
    private IndexRepository persistentIndexRepository;
    private MetadataRepository persistentMetadataRepository;
    public SearchService(QueryService queryService, HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository, MetadataRepository persistentMetadataRepository) {
        this.queryService = queryService;
        this.hazelcastInstance = hazelcastInstance;
        this.persistentIndexRepository = persistentIndexRepository;
        this.persistentMetadataRepository = persistentMetadataRepository;
    }

    public QueryResult searchWord(String word) {
        return queryService.search(word, hazelcastInstance, persistentIndexRepository);
    }

    public QueryResult fuzzySearch(String word, int tolerance) {
        return queryService.fuzzySearch(word, tolerance, hazelcastInstance, persistentIndexRepository);
    }

    public QueryResult metadataSearch(List<String> words, String from, String to, String author){
        return queryService.metadataSearch(words, from, to, author, hazelcastInstance, persistentMetadataRepository, persistentIndexRepository);
    }
}
