package adapters.external;

import domain.Book;
import domain.CrawlerService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GutenbergCrawlerAdapter implements CrawlerService {

    @Override
    public List<Book> fetchBooks(int startId, int n) {
        List<Book> books = new ArrayList<>();
        for (int i = startId; i < startId + n; i++) {
            try {
                URL url = new URL(String.format("https://www.gutenberg.org/cache/epub/%d/pg%d.txt", i, i));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    try (Scanner scanner = new Scanner(connection.getInputStream()).useDelimiter("\\A")) {
                        String content = scanner.hasNext() ? scanner.next() : "";
                        books.add(new Book(String.valueOf(i), content));
                    }
                } else {
                    System.out.println("Book ID " + i + " not found.");
                }
            } catch (IOException e) {
                System.err.println("Failed to fetch book with ID: " + i);
                e.printStackTrace();
            }
        }
        return books;
    }
}
