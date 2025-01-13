package application.update;

import application.load.HazelcastBookLoader;
import application.services.CreateIndexService;
import application.services.CreateMetadataService;
import application.tasks.TaskExecutor;
import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import domain.Book;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class DatamartUpdater {

    private final ISet<String> indexedBooks;
    private final HazelcastInstance hazelcastInstance;
    private final HazelcastBookLoader hazelcastBookLoader;
    private final CreateIndexService createIndexService;
    private final CreateMetadataService createMetadataService;

    public DatamartUpdater(ISet<String> indexedBooks, HazelcastInstance hazelcastInstance,
                           CreateIndexService createIndexService, CreateMetadataService createMetadataService) {
        this.indexedBooks = indexedBooks;
        this.hazelcastInstance = hazelcastInstance;
        this.hazelcastBookLoader = new HazelcastBookLoader(indexedBooks, hazelcastInstance);
        this.createIndexService = createIndexService;
        this.createMetadataService = createMetadataService;
    }

    public void updateDatamarts(TaskExecutor taskExecutor) {
        taskExecutor.submitTask(2, () -> {
            FencedLock lock = hazelcastInstance.getCPSubsystem().getLock("datalakeLock");
            lock.lock();
            try {
                List<Book> newBooks = hazelcastBookLoader.loadNewBooksFromHazelcast();
                if (!newBooks.isEmpty()) {
                    createIndexService.createAndUpdateInvertedIndex(newBooks);
                    createMetadataService.createAndUpdateMetadata(newBooks);
                    newBooks.forEach(book -> indexedBooks.add(book.getId()));
                    System.out.println("Datamart Updated...");
                    newBooks.forEach(book -> System.out.print(book.getId() + ". "));
                }
            } catch (IOException e) {
                System.err.println("Error updating datamarts: " + e.getMessage());
            } finally {
                lock.unlock();
            }
        });
    }
}
