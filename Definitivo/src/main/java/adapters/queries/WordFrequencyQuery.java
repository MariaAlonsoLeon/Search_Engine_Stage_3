package adapters.queries;

import com.hazelcast.collection.IList;
import com.hazelcast.map.IMap;
import domain.*;

import java.util.List;
import java.util.stream.Collectors;

public class WordFrequencyQuery implements Query {

    private final IMap<String, InvertedIndex> invertedIndex;
    private final IndexRepository persistentIndexRepository;

    public WordFrequencyQuery(IMap<String, InvertedIndex> invertedIndex, IndexRepository persistentIndexRepository) {
        this.invertedIndex = invertedIndex;
        this.persistentIndexRepository = persistentIndexRepository;
    }

    @Override
    public QueryResult execute() {
        InvertedIndex globalIndex = invertedIndex.get("globalIndex");

        if (globalIndex == null || globalIndex.getIndex().isEmpty()) {
            return new QueryResult(List.of(), 0, "word_frequency");
        }

        List<String> results = globalIndex.getIndex().entrySet().stream()
                .map(entry -> {
                    int frequency = entry.getValue().values().stream()
                            .mapToInt(InvertedIndex.WordData::getFrequency)
                            .sum();
                    return String.format("{word: %s, frequency: %d}", entry.getKey(), frequency);
                })
                .collect(Collectors.toList());

        return new QueryResult(results, results.size(), "word_frequency");
    }
}
