package application.load;

import domain.Book;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BooksDatalakeLoader {

    public static List<Book> loadBooksFromDatalake() throws IOException {
        List<Book> books = new ArrayList<>();
        Path datalakePath = Paths.get("datalake");

        if (!Files.exists(datalakePath)) {
            System.out.println("No datalake found. Skipping index generation.");
            return books;
        }

        Files.walk(datalakePath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".txt"))
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        String bookId = path.getFileName().toString().replace("book_", "").replace(".txt", "");
                        books.add(new Book(bookId, content));
                    } catch (IOException e) {
                        System.err.println("Error reading book: " + path + " - " + e.getMessage());
                    }
                });

        System.out.println("Loaded " + books.size() + " books from datalake.");
        return books;
    }

}
