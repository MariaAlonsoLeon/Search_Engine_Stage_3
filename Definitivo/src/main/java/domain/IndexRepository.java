package domain;

public interface IndexRepository {
    void saveInvertedIndex(InvertedIndex invertedIndex);

    InvertedIndex findInvertedIndex();
}