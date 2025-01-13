package application.load;

import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import domain.Book;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HazelcastBookLoader {
    private final ISet<String> indexedBooks;
    private static HazelcastInstance hazelcastInstance;

    public HazelcastBookLoader(ISet<String> indexedBooks, HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.indexedBooks = indexedBooks;
    }

    public List<Book> loadNewBooksFromHazelcast() throws IOException {
        List<Book> newBooks = new ArrayList<>();

        IMap<String, String> datalakeMap = hazelcastInstance.getMap("datalake");

        for (String bookId : datalakeMap.keySet()) {
            if (!indexedBooks.contains(bookId)) {
                String content = datalakeMap.get(bookId);
                newBooks.add(new Book(bookId, content));
            }
        }

        return newBooks;
    }
}
