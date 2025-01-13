package benchmark;

import domain.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final String[] WORD_POOL = {
            "search", "engine", "query", "data", "index", "performance", "test", "database",
            "mongo", "neo4j", "repository", "benchmark", "java", "jmh", "hazelcast"
    };

    public static List<Book> generateBooks(int numBooks, int wordsPerBook) {
        Random random = new Random();
        List<Book> books = new ArrayList<>();

        for (int i = 1; i <= numBooks; i++) {
            StringBuilder content = new StringBuilder();
            for (int j = 0; j < wordsPerBook; j++) {
                content.append(WORD_POOL[random.nextInt(WORD_POOL.length)]).append(" ");
            }
            books.add(new Book(String.valueOf(i), content.toString().trim()));
        }
        return books;
    }

    public static List<String> generateQueries(int numQueries, int maxWordsPerQuery) {
        Random random = new Random();
        List<String> queries = new ArrayList<>();

        for (int i = 0; i < numQueries; i++) {
            int numWords = random.nextInt(maxWordsPerQuery) + 1;
            StringBuilder query = new StringBuilder();
            for (int j = 0; j < numWords; j++) {
                query.append(WORD_POOL[random.nextInt(WORD_POOL.length)]).append(" ");
            }
            queries.add(query.toString().trim());
        }
        return queries;
    }
}