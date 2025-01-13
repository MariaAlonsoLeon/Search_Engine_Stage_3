package adapters.persistence;

import domain.IndexRepository;
import domain.InvertedIndex;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileIndexRepository implements IndexRepository {

    private final String filePath;

    public FileIndexRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void saveInvertedIndex(InvertedIndex newIndex) {
        InvertedIndex currentIndex = loadFromFile();

        mergeIndexes(currentIndex, newIndex);

        saveToDisk(currentIndex);
    }

    @Override
    public InvertedIndex findInvertedIndex() {
        return loadFromFile();
    }

    private void saveToDisk(InvertedIndex invertedIndex) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
            for (var wordEntry : invertedIndex.getIndex().entrySet()) {
                String word = wordEntry.getKey();
                String booksData = wordEntry.getValue().entrySet().stream()
                        .map(bookEntry -> {
                            String bookId = bookEntry.getKey();
                            InvertedIndex.WordData wordData = bookEntry.getValue();
                            String positions = wordData.getPositions().stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(","));
                            return String.format("%s:[%s]#%d", bookId, positions, wordData.getFrequency());
                        })
                        .collect(Collectors.joining(";"));
                writer.write(String.format("%s|%s%n", word, booksData));
            }
            System.out.println("Inverted index saved to text file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InvertedIndex loadFromFile() {
        InvertedIndex invertedIndex = new InvertedIndex();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("|")) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                String[] parts = line.split("\\|", 2);
                if (parts.length < 2 || parts[1].isEmpty()) {
                    System.err.println("Skipping incomplete line: " + line);
                    continue;
                }

                String word = parts[0].trim();
                String[] booksData = parts[1].split(";");

                for (String bookData : booksData) {
                    if (!bookData.contains(":")) {
                        System.err.println("Skipping malformed book data: " + bookData);
                        continue;
                    }

                    String[] bookParts = bookData.split(":", 2);
                    if (bookParts.length < 2 || bookParts[1].isEmpty()) {
                        System.err.println("Skipping malformed book parts: " + bookData);
                        continue;
                    }

                    String bookId = bookParts[0].trim();

                    String[] positionAndFrequency = bookParts[1].split("#");
                    if (positionAndFrequency.length != 2) {
                        System.err.println("Skipping malformed position and frequency data: " + bookParts[1]);
                        continue;
                    }

                    try {
                        List<Integer> positions = Arrays.stream(
                                        positionAndFrequency[0]
                                                .replace("[", "")
                                                .replace("]", "")
                                                .split(","))
                                .filter(pos -> !pos.isEmpty())
                                .map(Integer::parseInt)
                                .toList();

                        int frequency = Integer.parseInt(positionAndFrequency[1].trim());
                        invertedIndex.addWord(word, bookId, positions);
                        invertedIndex.getIndex().get(word).get(bookId).setFrequency(frequency);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid number format in: " + bookParts[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return invertedIndex;
    }

    private void mergeIndexes(InvertedIndex currentIndex, InvertedIndex newIndex) {
        newIndex.getIndex().forEach((word, newBookData) -> {
            currentIndex.getIndex().merge(word, newBookData, (existingBooks, updatedBooks) -> {
                updatedBooks.forEach((bookId, newWordData) -> {
                    existingBooks.merge(bookId, newWordData, (existingWordData, additionalWordData) -> {
                        existingWordData.getPositions().addAll(additionalWordData.getPositions());
                        existingWordData.setFrequency(existingWordData.getFrequency() + additionalWordData.getFrequency());
                        return existingWordData;
                    });
                });
                return existingBooks;
            });
        });

    }
}
