package adapters.queries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import domain.IndexRepository;
import domain.InvertedIndex;
import domain.Query;
import domain.QueryResult;
import application.utils.InvertedIndexUnifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FuzzyQuery implements Query {
    private final String word;
    private final int tolerance;
    private final IMap<String, InvertedIndex> hazelcastIndex;
    private final IndexRepository persistentIndexRepository;
    private final InvertedIndexUnifier invertedIndexUnifier;

    public FuzzyQuery(String word, int tolerance, HazelcastInstance hazelcastInstance, IndexRepository persistentIndexRepository) {
        this.word = word;
        this.tolerance = tolerance;
        this.hazelcastIndex = hazelcastInstance.getMap("invertedIndex");
        this.persistentIndexRepository = persistentIndexRepository;
        this.invertedIndexUnifier = new InvertedIndexUnifier(hazelcastInstance, persistentIndexRepository);
    }

    @Override
    public QueryResult execute() {
        InvertedIndex unifiedIndex = invertedIndexUnifier.unifyIndexes();

        List<String> matchingWords = unifiedIndex.getIndex().keySet().stream()
                .filter(key -> calculateLevenshteinDistance(key, word.toLowerCase()) <= tolerance)
                .toList();

        List<FuzzyResult> fuzzyResults = new ArrayList<>();
        for (String matchedWord : matchingWords) {
            Map<String, InvertedIndex.WordData> bookData = unifiedIndex.getIndex().get(matchedWord);
            List<QueryResult.BookInfo> books = new ArrayList<>();
            bookData.forEach((bookId, wordData) -> books.add(new QueryResult.BookInfo(
                    bookId,
                    wordData.getPositions(),
                    wordData.getFrequency()
            )));
            fuzzyResults.add(new FuzzyResult(matchedWord, books));
        }

        return new ExtendedFuzzyQueryResult(fuzzyResults, matchingWords.size(), "FuzzyQuery");
    }

    private int calculateLevenshteinDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];
        for (int i = 0; i <= word1.length(); i++) {
            for (int j = 0; j <= word2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        return dp[word1.length()][word2.length()];
    }

    public static class FuzzyResult {
        private final String word;
        private final List<QueryResult.BookInfo> books;

        public FuzzyResult(String word, List<QueryResult.BookInfo> books) {
            this.word = word;
            this.books = books;
        }

        public String getWord() {
            return word;
        }

        public List<QueryResult.BookInfo> getBooks() {
            return books;
        }
    }

    public static class ExtendedFuzzyQueryResult extends QueryResult {
        private final List<FuzzyResult> fuzzyResults;

        public ExtendedFuzzyQueryResult(List<FuzzyResult> fuzzyResults, int totalResults, String queryType) {
            super(new ArrayList<>(), totalResults, queryType);
            this.fuzzyResults = fuzzyResults;
        }

        @Override
        public String toJson() {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(fuzzyResults);
        }
    }
}
