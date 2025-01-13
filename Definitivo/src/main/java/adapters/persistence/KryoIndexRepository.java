package adapters.persistence;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import domain.IndexRepository;
import domain.InvertedIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class KryoIndexRepository implements IndexRepository {
    private final String filePath;
    private final Kryo kryo;

    public KryoIndexRepository(String filePath) {
        this.filePath = filePath;
        this.kryo = new Kryo();
        this.kryo.register(InvertedIndex.class);
        this.kryo.register(InvertedIndex.WordData.class);
        this.kryo.register(java.util.HashMap.class);
        this.kryo.register(java.util.ArrayList.class);
    }

    @Override
    public void saveInvertedIndex(InvertedIndex newIndex) {
        InvertedIndex existingIndex = findInvertedIndex();

        mergeInvertedIndexes(existingIndex, newIndex);

        try (Output output = new Output(new FileOutputStream(filePath))) {
            kryo.writeObject(output, existingIndex);
            System.out.println("Inverted index saved to kryo file");
        } catch (IOException e) {
            System.err.println("Error al guardar el índice invertido: " + e.getMessage());
        }
    }

    @Override
    public InvertedIndex findInvertedIndex() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new InvertedIndex();
        }

        try (Input input = new Input(new FileInputStream(file))) {
            return kryo.readObject(input, InvertedIndex.class);
        } catch (Exception e) {
            System.err.println("Error saving the inverted index: " + e.getMessage());
            return new InvertedIndex();
        }
    }

    private void mergeInvertedIndexes(InvertedIndex existingIndex, InvertedIndex newIndex) {
        for (Map.Entry<String, Map<String, InvertedIndex.WordData>> wordEntry : newIndex.getIndex().entrySet()) {
            String word = wordEntry.getKey();
            Map<String, InvertedIndex.WordData> newWordDataMap = wordEntry.getValue();

            Map<String, InvertedIndex.WordData> existingWordDataMap = existingIndex.getIndex()
                    .computeIfAbsent(word, k -> new java.util.HashMap<>());

            for (Map.Entry<String, InvertedIndex.WordData> bookEntry : newWordDataMap.entrySet()) {
                String bookId = bookEntry.getKey();
                InvertedIndex.WordData newWordData = bookEntry.getValue();

                existingWordDataMap.computeIfAbsent(bookId, k -> new InvertedIndex.WordData())
                        .addPositions(newWordData.getPositions());
            }
        }
    }
}
