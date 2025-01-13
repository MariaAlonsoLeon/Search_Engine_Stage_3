package application.utils;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import domain.IndexRepository;
import domain.InvertedIndex;

import java.util.Map;

public class InvertedIndexUnifier {

    private final IMap<String, InvertedIndex> hazelcastIndex;
    private final IndexRepository persistentIndexRepository;

    public InvertedIndexUnifier(HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository) {
        this.hazelcastIndex = hazelcastInstance.getMap("invertedIndex");
        this.persistentIndexRepository = persistentIndexRepository;
    }

    public InvertedIndex unifyIndexes() {
        InvertedIndex unifiedIndex = new InvertedIndex();

        InvertedIndex hazelcastData = hazelcastIndex.get("globalIndex");
        if (hazelcastData != null) {
            hazelcastData.getIndex().forEach((word, bookData) -> {
                unifiedIndex.getIndex().merge(word, bookData, this::mergeBookData);
            });
        }

        InvertedIndex persistentData = persistentIndexRepository.findInvertedIndex();
        persistentData.getIndex().forEach((word, bookData) -> {
            unifiedIndex.getIndex().merge(word, bookData, this::mergeBookData);
        });

        return unifiedIndex;
    }

    private Map<String, InvertedIndex.WordData> mergeBookData(
            Map<String, InvertedIndex.WordData> existingBooks,
            Map<String, InvertedIndex.WordData> newBooks
    ) {
        newBooks.forEach((bookId, newWordData) ->
                existingBooks.merge(bookId, newWordData, (existingWordData, updatedWordData) -> {
                    existingWordData.getPositions().addAll(updatedWordData.getPositions());
                    existingWordData.setFrequency(
                            existingWordData.getFrequency() + updatedWordData.getFrequency()
                    );
                    return existingWordData;
                }));
        return existingBooks;
    }
}
