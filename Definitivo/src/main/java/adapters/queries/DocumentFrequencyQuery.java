package adapters.queries;

import com.hazelcast.map.IMap;
import domain.InvertedIndex;
import domain.Query;
import domain.QueryResult;

import java.util.List;
import java.util.stream.Collectors;


public class DocumentFrequencyQuery implements Query {

    private final IMap<String, InvertedIndex> invertedIndex;

    public DocumentFrequencyQuery(IMap<String, InvertedIndex> invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    @Override
    public QueryResult execute() {
        InvertedIndex index = invertedIndex.get("globalIndex");

        List<String> results = index.getIndex().entrySet().stream()
                .map(entry -> {
                    String word = entry.getKey();
                    int docCount = entry.getValue().size();
                    return String.format("{word:%s, document_count:%d}", word, docCount);
                })
                .collect(Collectors.toList());

        return new QueryResult(results, results.size(), "doc_frequency");
    }
}
