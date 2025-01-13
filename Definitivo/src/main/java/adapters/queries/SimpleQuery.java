package adapters.queries;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import domain.IndexRepository;
import domain.InvertedIndex;
import domain.Query;
import domain.QueryResult;
import application.utils.InvertedIndexUnifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleQuery implements Query {
    private final String word;
    private final IMap<String, InvertedIndex> hazelcastIndex;
    private final IndexRepository persistentIndexRepository;
    private final InvertedIndexUnifier invertedIndexUnifier;

    public SimpleQuery(String word, HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository) {
        this.word = word;
        this.hazelcastIndex = hazelcastInstance.getMap("invertedIndex");
        this.persistentIndexRepository = persistentIndexRepository;
        this.invertedIndexUnifier = new InvertedIndexUnifier(hazelcastInstance, persistentIndexRepository);
    }

    @Override
    public QueryResult execute() {
        InvertedIndex unifiedIndex = invertedIndexUnifier.unifyIndexes();

        List<String> books = new ArrayList<>();
        if (unifiedIndex.getIndex().containsKey(word.toLowerCase())) {
            var bookData = unifiedIndex.getIndex().get(word.toLowerCase());
            bookData.forEach((bookId, wordData) -> books.add(new QueryResult.BookInfo(
                            bookId,
                            new ArrayList<>(wordData.getPositions()),
                            wordData.getFrequency()
                    ).toString()
            ));
        }

        return new QueryResult(books, books.size(), "SimpleQuery");
    }

}
