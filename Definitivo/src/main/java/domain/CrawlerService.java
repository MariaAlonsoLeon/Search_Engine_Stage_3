package domain;

import java.util.List;

public interface CrawlerService {
    List<Book> fetchBooks(int startId, int n);
}