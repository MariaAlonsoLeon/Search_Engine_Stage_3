package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Map<String, WordData>> index;

    public InvertedIndex() {
        this.index = new HashMap<>();
    }

    public Map<String, Map<String, WordData>> getIndex() {
        return index;
    }

    public void addWord(String word, String bookId, List<Integer> positions) {
        index.computeIfAbsent(word, k -> new HashMap<>())
                .computeIfAbsent(bookId, k -> new WordData())
                .addPositions(positions);
    }

    public Map<String, WordData> getWord(String word) {
        Map<String, WordData> indexing = index.get(word);
        return indexing;
    }

    public void setIndex(Map<String, Map<String, WordData>> indexNew) {
        index = indexNew;
    }

    public static class WordData implements Serializable {
        private static final long serialVersionUID = 1L;

        private int frequency;
        private List<Integer> positions;

        public WordData() {
            this.frequency = 0;
            this.positions = new ArrayList<>();
        }

        public void addPositions(List<Integer> newPositions) {
            if (positions == null) {
                positions = newPositions;
            } else {
                positions.addAll(newPositions);
            }
            frequency += newPositions.size();
        }

        public int getFrequency() {
            return frequency;
        }

        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }

        public List<Integer> getPositions() {
            return positions;
        }

        public void setPositions(List<Integer> positions) {
            this.positions = positions;
        }
    }
}
