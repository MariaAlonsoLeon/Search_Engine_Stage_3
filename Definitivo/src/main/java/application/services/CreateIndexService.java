package application.services;

import domain.Book;
import domain.IndexRepository;
import domain.InvertedIndex;

import java.util.*;
import java.util.regex.Pattern;

public class CreateIndexService {

    private final IndexRepository indexRepository;
    private final Set<String> stopwords;

    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("^[a-z]{2,}$");

    public CreateIndexService(IndexRepository indexRepository, Set<String> stopwords) {
        this.indexRepository = indexRepository;
        this.stopwords = stopwords;
    }

    public void createAndUpdateInvertedIndex(List<Book> books) {
        InvertedIndex invertedIndex = new InvertedIndex();
        for (Book book : books) {
            String[] words = book.getContent().toLowerCase().split("\\W+");
            Map<String, List<Integer>> wordPositions = new HashMap<>();

            for (int i = 0; i < words.length; i++) {
                String word = words[i];

                if (isValidWord(word)) {
                    wordPositions.computeIfAbsent(word, k -> new ArrayList<>()).add(i);
                }
            }

            wordPositions.forEach((word, positions) ->
                    invertedIndex.addWord(word, book.getId(), positions));
        }
        indexRepository.saveInvertedIndex(invertedIndex);
    }

    private boolean isValidWord(String word) {
        return !stopwords.contains(word) &&
                VALID_WORD_PATTERN.matcher(word).matches();
    }
}
