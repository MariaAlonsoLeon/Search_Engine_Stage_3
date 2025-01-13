package adapters.queries;

import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import domain.IndexRepository;
import domain.InvertedIndex;
import domain.Metadata;
import domain.MetadataRepository;
import domain.Query;
import domain.QueryResult;
import application.utils.InvertedIndexUnifier;
import application.utils.MetadataUnifier;

import java.util.*;
import java.util.stream.Collectors;

public class MetadataQuery implements Query {
    private final List<String> words;
    private final String from;
    private final String to;
    private final String author;
    private final IList<Metadata> hazelcastMetadataList;
    private final MetadataRepository persistentMetadataRepository;
    private final IMap<String, InvertedIndex> hazelcastIndex;
    private final IndexRepository persistentIndexRepository;
    private final MetadataUnifier metadataUnifier;
    private final InvertedIndexUnifier invertedIndexUnifier;

    public MetadataQuery(List<String> words, String from, String to, String author,
                         HazelcastInstance hazelcastInstance,
                         MetadataRepository persistentMetadataRepository,
                         IndexRepository persistentIndexRepository) {
        this.words = words;
        this.from = from;
        this.to = to;
        this.author = author;
        this.hazelcastMetadataList = hazelcastInstance.getList("metadata");
        this.persistentMetadataRepository = persistentMetadataRepository;
        this.hazelcastIndex = hazelcastInstance.getMap("invertedIndex");
        this.persistentIndexRepository = persistentIndexRepository;
        this.metadataUnifier = new MetadataUnifier(hazelcastMetadataList, persistentMetadataRepository);
        this.invertedIndexUnifier = new InvertedIndexUnifier(hazelcastInstance, persistentIndexRepository);
    }

    @Override
    public QueryResult execute() {
        List<Metadata> unifiedMetadata = metadataUnifier.unifyMetadata();
        InvertedIndex unifiedIndex = invertedIndexUnifier.unifyIndexes();

        List<Metadata> filteredMetadata = unifiedMetadata.stream()
                .filter(metadata -> (words == null || containsWord(metadata, unifiedIndex, words)) &&
                        (from == null || metadata.getDate().compareTo(from) >= 0) &&
                        (to == null || metadata.getDate().compareTo(to) <= 0) &&
                        (author == null || metadata.getAuthor().equalsIgnoreCase(author)))
                .toList();


        List<String> books = filteredMetadata.stream()
                .map(metadata -> new QueryResult.BookInfo(
                        metadata.getBookId(),
                        getPositionsFromIndex(metadata.getBookId(), unifiedIndex, words),
                        getFrequencyFromIndex(metadata.getBookId(), unifiedIndex, words)
                ).toString())
                .toList();

        return new QueryResult(books, books.size(), "MetadataQuery");
    }

    private List<Metadata> unifyMetadata() {
        Map<String, Metadata> metadataMap = new HashMap<>();

        for (Metadata metadata : hazelcastMetadataList) {
            metadataMap.put(metadata.getBookId(), metadata);
        }

        for (Metadata metadata : persistentMetadataRepository.findAllMetadata()) {
            metadataMap.put(metadata.getBookId(), metadata);
        }

        return new ArrayList<>(metadataMap.values());
    }


    private InvertedIndex unifyIndexes() {
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

    private boolean containsWord(Metadata metadata, InvertedIndex index, List<String> words) {
        for (String word : words) {
            if (index.getIndex().containsKey(word.toLowerCase()) &&
                    index.getIndex().get(word.toLowerCase()).containsKey(metadata.getBookId())) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> getPositionsFromIndex(String bookId, InvertedIndex index, List<String> words) {
        return words.stream()
                .filter(word -> index.getIndex().containsKey(word.toLowerCase()))
                .flatMap(word -> index.getIndex().get(word.toLowerCase()).entrySet().stream())
                .filter(entry -> entry.getKey().equals(bookId))
                .flatMap(entry -> entry.getValue().getPositions().stream())
                .collect(Collectors.toList());
    }

    private int getFrequencyFromIndex(String bookId, InvertedIndex index, List<String> words) {
        return words.stream()
                .filter(word -> index.getIndex().containsKey(word.toLowerCase()))
                .flatMap(word -> index.getIndex().get(word.toLowerCase()).entrySet().stream())
                .filter(entry -> entry.getKey().equals(bookId))
                .mapToInt(entry -> entry.getValue().getFrequency())
                .sum();
    }
}
