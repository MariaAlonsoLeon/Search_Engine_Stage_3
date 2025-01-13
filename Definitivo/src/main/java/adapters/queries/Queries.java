package adapters.queries;

import com.hazelcast.core.HazelcastInstance;
import domain.IndexRepository;
import domain.MetadataRepository;
import domain.QueryResult;
import domain.QueryService;

import java.util.List;

public class Queries implements QueryService {

    @Override
    public QueryResult search(String word, HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository) {
        SimpleQuery simpleQuery = new SimpleQuery(word, hazelcastInstance, persistentIndexRepository);
        QueryResult result = simpleQuery.execute();
        return result;
    }

    @Override
    public QueryResult fuzzySearch(String word, int tolerance, HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository) {
        FuzzyQuery fuzzyQuery = new FuzzyQuery(word, tolerance, hazelcastInstance, persistentIndexRepository);
        QueryResult result = fuzzyQuery.execute();
        return result;
    }

    @Override
    public QueryResult metadataSearch(List<String> words, String from, String to, String author,
                                      HazelcastInstance hazelcastInstance,
                                      MetadataRepository persistentMetadataRepository,
                                      IndexRepository persistentIndexRepository){
        MetadataQuery metadataQuery = new MetadataQuery(words, from, to, author, hazelcastInstance, persistentMetadataRepository, persistentIndexRepository);
        QueryResult result = metadataQuery.execute();
        return result;
    }
}
