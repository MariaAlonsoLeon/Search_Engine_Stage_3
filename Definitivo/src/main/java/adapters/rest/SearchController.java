package adapters.rest;

import adapters.persistence.FileIndexRepository;
import adapters.queries.*;
import application.services.SearchService;
import com.google.gson.Gson;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import domain.*;
import spark.Spark;

import java.util.*;
import java.util.stream.Collectors;

public class SearchController {
    private final SearchService searchService;
    private final HazelcastInstance hazelcastInstance;
    private final Gson gson = new Gson();
    private final MetadataRepository metadataRepository;
    private final IndexRepository indexRepository;

    public SearchController(SearchService searchService, HazelcastInstance hazelcastInstance, MetadataRepository metadataRepository, IndexRepository indexRepository) {
        this.searchService = searchService;
        this.hazelcastInstance = hazelcastInstance;
        this.metadataRepository = metadataRepository;
        this.indexRepository = indexRepository;
        setupRoutes();
    }

    private void setupRoutes() {
        Spark.get("/search/simple", (req, res) -> {
            String word = req.queryParams("word");
            res.type("application/json");
            return gson.toJson(searchService.searchWord(word));
        });


        Spark.get("/search/fuzzy", (req, res) -> {
            String word = req.queryParams("word");
            int tolerance = Integer.parseInt(req.queryParams("tolerance"));
            res.type("application/json");
            return gson.toJson(searchService.fuzzySearch(word, tolerance));
        });

        Spark.get("/documents/:words", (req, res) -> {
            String wordsParam = req.params(":words");
            List<String> words = List.of(wordsParam.split("\\+"));
            String from = req.queryParams("from");
            String to = req.queryParams("to");
            String author = req.queryParams("author");

            res.type("application/json");
            return gson.toJson(searchService.metadataSearch(words, from, to, author));
        });


        Spark.get("/books/datalake", (req, res) -> {
            res.type("application/json");
            return gson.toJson(hazelcastInstance.getMap("datalake").size());
        });

        Spark.get("/books/invertedindex", (req, res) -> {
            res.type("application/json");
            List<String> bookIds = listBookIdsInHazelcastInvertedIndex();
            return gson.toJson(bookIds);
        });

        Spark.get("/books/invertedindexALL", (req, res) -> {
            res.type("application/json");
            List<String> bookIds = listBookIdsInHazelcastInvertedIndex();
            Set<String> bookIds_Disk = getBookIds();
            Set<String> mergedBookIds = new HashSet<>(bookIds);
            mergedBookIds.addAll(bookIds_Disk);

            return gson.toJson(mergedBookIds);
        });

        Spark.get("/books/metadata", (req, res) -> {
            res.type("application/json");
            return gson.toJson(hazelcastInstance.getList("metadata").size());
        });

        Spark.get("/stats/:type", (req, res) -> {
            String type = req.params(":type");

            QueryResult result;
            switch (type) {
                case "doc_frequency":
                    result = new DocumentFrequencyQuery(hazelcastInstance.getMap("invertedIndex")).execute();
                    break;
                    case "top_authors":
                    result = new TopAuthorsQuery(hazelcastInstance.getList("metadata"), metadataRepository).execute();
                    break;
                case "language_distribution":
                    result = new LanguageDistributionQuery(hazelcastInstance.getList("metadata"), metadataRepository).execute();
                    break;
                case "release_date_range":
                    result = new ReleaseDateRangeQuery(hazelcastInstance.getList("metadata"), metadataRepository).execute();
                    break;
                case "word_frequency":
                    result = new WordFrequencyQuery(hazelcastInstance.getMap("invertedIndex"), indexRepository).execute();
                    break;
                default:
                    res.status(400);
                    return "Invalid stats type. Supported types: doc_frequency, top_authors, language_distribution, release_date_range, word_frequency";
            }

            res.type("application/json");

            return gson.toJson((result));
        });
    }

    private List<String> listBookIdsInHazelcastInvertedIndex() {
        List<String> bookIds = new ArrayList<>();
        IMap<String, InvertedIndex> invertedIndexMap = hazelcastInstance.getMap("invertedIndex");
        InvertedIndex invertedIndex = invertedIndexMap.get("globalIndex");

        if (invertedIndex != null) {
            invertedIndex.getIndex().forEach((word, bookData) -> {
                bookData.forEach((bookId, wordData) -> {
                    if (!bookIds.contains(bookId)) {
                        bookIds.add(bookId);
                    }
                });
            });
        }

        return bookIds;
    }

    public Set<String> getBookIds() {
        FileIndexRepository repository = new FileIndexRepository("invertedIndex.txt");

        InvertedIndex invertedIndex = repository.findInvertedIndex();

        return invertedIndex.getIndex().values().stream()
                .flatMap(bookData -> bookData.keySet().stream())
                .collect(Collectors.toSet());
    }

}
