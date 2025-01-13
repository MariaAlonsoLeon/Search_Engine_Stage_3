package adapters.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import domain.IndexRepository;
import domain.InvertedIndex;

public class HazelcastIndexRepository implements IndexRepository {
    private final IMap<String, InvertedIndex> hazelcastIndexMap;

    public HazelcastIndexRepository(HazelcastInstance hazelcastInstance) {
        this.hazelcastIndexMap = hazelcastInstance.getMap("invertedIndex");
    }

    @Override
    public void saveInvertedIndex(InvertedIndex invertedIndex) {
        InvertedIndex currentIndex = hazelcastIndexMap.getOrDefault("globalIndex", new InvertedIndex());

        mergeIndexes(currentIndex, invertedIndex);

        hazelcastIndexMap.put("globalIndex", currentIndex);
    }

    @Override
    public InvertedIndex findInvertedIndex() {
        return hazelcastIndexMap.getOrDefault("globalIndex", new InvertedIndex());
    }

    private void mergeIndexes(InvertedIndex currentIndex, InvertedIndex newIndex) {
        newIndex.getIndex().forEach((word, bookData) -> {
            currentIndex.getIndex().merge(word, bookData, (existingBooks, newBooks) -> {
                newBooks.forEach((bookId, newWordData) ->
                        existingBooks.merge(bookId, newWordData, (existingWordData, updatedWordData) -> {
                            existingWordData.getPositions().addAll(updatedWordData.getPositions());
                            existingWordData.setFrequency(
                                    existingWordData.getFrequency() + updatedWordData.getFrequency()
                            );
                            return existingWordData;
                        }));
                return existingBooks;
            });
        });
    }
}