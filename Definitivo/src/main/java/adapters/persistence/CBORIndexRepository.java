package adapters.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import domain.IndexRepository;
import domain.InvertedIndex;

import java.io.File;
import java.io.IOException;

public class CBORIndexRepository implements IndexRepository {
    private final File file;
    private final ObjectMapper objectMapper;

    public CBORIndexRepository(String filePath) {
        this.file = new File(filePath);
        this.objectMapper = new ObjectMapper(new CBORFactory());
    }

    @Override
    public void saveInvertedIndex(InvertedIndex newIndex) {
        InvertedIndex currentIndex = loadFromFile();
        mergeIndexes(currentIndex, newIndex);
        saveToFile(currentIndex);
    }

    @Override
    public InvertedIndex findInvertedIndex() {
        return loadFromFile();
    }

    private void saveToFile(InvertedIndex invertedIndex) {
        try {
            objectMapper.writeValue(file, invertedIndex);
            System.out.println("Inverted index successfully saved to CBOR file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InvertedIndex loadFromFile() {
        if (!file.exists() || file.length() == 0) {
            return new InvertedIndex();
        }
        try {
            return objectMapper.readValue(file, InvertedIndex.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new InvertedIndex();
        }
    }

    private void mergeIndexes(InvertedIndex currentIndex, InvertedIndex newIndex) {
        newIndex.getIndex().forEach((word, newBookData) ->
                currentIndex.getIndex().merge(word, newBookData, (existingBooks, updatedBooks) -> {
                    updatedBooks.forEach((bookId, newWordData) ->
                            existingBooks.merge(bookId, newWordData, (existingWordData, additionalWordData) -> {
                                existingWordData.getPositions().addAll(additionalWordData.getPositions());
                                existingWordData.setFrequency(existingWordData.getFrequency() + additionalWordData.getFrequency());
                                return existingWordData;
                            })
                    );
                    return existingBooks;
                })
        );
    }
}
