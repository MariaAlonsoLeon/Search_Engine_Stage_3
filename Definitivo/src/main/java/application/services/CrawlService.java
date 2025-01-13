package application.services;

import application.tasks.TaskExecutor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.map.IMap;
import domain.Book;
import domain.CrawlerService;
import com.hazelcast.cp.lock.FencedLock;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class CrawlService {

    private final CrawlerService crawlerService;
    private final String datalakePath = "datalake";
    private final IMap<String, String> hazelcastBookMap;
    private final IMap<String, Boolean> downloadedBooksMap;
    private final IAtomicLong globalBookCounter;
    private final HazelcastInstance hazelcastInstance;

    public CrawlService(CrawlerService crawlerService, HazelcastInstance hazelcastInstance) {
        this.crawlerService = crawlerService;
        this.hazelcastInstance = hazelcastInstance;
        this.hazelcastBookMap = hazelcastInstance.getMap("datalake");
        this.downloadedBooksMap = hazelcastInstance.getMap("downloadedBooks");
        this.globalBookCounter = hazelcastInstance.getCPSubsystem().getAtomicLong("globalBookCounter");
    }

    public void startCrawling(int booksPerNode, TaskExecutor taskExecutor) {
        taskExecutor.submitTask(1, () -> {
            FencedLock lock = hazelcastInstance.getCPSubsystem().getLock("datalakeLock");
            lock.lock();
            try {
                long startId = globalBookCounter.getAndAdd(booksPerNode) + 1;

                List<Book> books = crawlerService.fetchBooks((int) startId, booksPerNode);

                books.forEach(book -> {
                    if (downloadedBooksMap.putIfAbsent(book.getId(), true) == null) {
                        hazelcastBookMap.put(book.getId(), book.getContent());
                        saveBookToDatalake(book);
                        System.out.println("Book saved locally: " + book.getId());
                    }
                });
            } finally {
                lock.unlock();
            }
        });
    }

    private void saveBookToDatalake(Book book) {
        String date = getCurrentDate();
        String folderPath = datalakePath + "/" + date;
        String filePath = folderPath + "/book_" + book.getId() + ".txt";
        try {
            Files.createDirectories(Paths.get(folderPath));
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(book.getContent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }
}
