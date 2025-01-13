package domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryResult {
    private List<String> books;
    private int totalResults;
    private String queryType;

    public QueryResult(List<String> books, int totalResults, String queryType) {
        this.books = books;
        this.totalResults = totalResults;
        this.queryType = queryType;
    }

    public List<String> getBooks() {
        return books;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public String getQueryType() {
        return queryType;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static class BookInfo {
        private String bookId;
        private List<Integer> positions;
        private int frequency;

        public BookInfo(String bookId, List<Integer> positions, int frequency) {
            this.bookId = bookId;
            this.positions = positions;
            this.frequency = frequency;
        }

        public String getBookId() {
            return bookId;
        }

        public List<Integer> getPositions() {
            return positions;
        }

        public int getFrequency() {
            return frequency;
        }

        @Override
        public String toString() {
            Set<Integer> uniquePositions = positions.stream().collect(Collectors.toSet());
            return "bookId='" + bookId + '\'' +
                    ", positions=" + uniquePositions +
                    ", frequency=" + frequency;
        }
    }
}
