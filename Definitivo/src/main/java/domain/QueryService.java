package domain;

import com.hazelcast.core.HazelcastInstance;

import java.util.List;

public interface QueryService {
    QueryResult search(String word, HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository);
    QueryResult fuzzySearch(String word, int tolerance, HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository);
    QueryResult metadataSearch(List<String> words, String from, String to, String author,
                               HazelcastInstance hazelcastInstance, MetadataRepository persistentMetadataRepository, IndexRepository persistentIndexRepository);

}
