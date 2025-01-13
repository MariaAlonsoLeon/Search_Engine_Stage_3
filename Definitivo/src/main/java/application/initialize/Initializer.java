package application.initialize;

import adapters.external.GutenbergCrawlerAdapter;
import adapters.persistence.HazelcastIndexRepository;
import adapters.persistence.HazelcastMetadataRepository;
import adapters.queries.Queries;
import adapters.rest.SearchController;
import application.load.BooksDatalakeLoader;
import application.services.CrawlService;
import application.services.CreateIndexService;
import application.services.CreateMetadataService;
import application.services.SearchService;
import application.utils.DataTypeSelector;
import application.utils.StopwordsLoader;
import com.hazelcast.collection.ISet;
import com.hazelcast.core.HazelcastInstance;
import config.HazelcastConfig;
import domain.*;

import java.io.IOException;
import java.util.List;

public class Initializer {
    private HazelcastInstance hazelcastInstance;
    private IndexRepository diskInvertedIndexRepository;
    private MetadataRepository diskMetadataRepository;
    private IndexRepository indexRepository;
    private MetadataRepository metadataRepository;
    private CrawlService crawlService;
    private CreateIndexService createIndexService;
    private CreateMetadataService createMetadataService;
    private ISet<String> indexedBooks;

    public Initializer(DataTypeSelector dataTypeSelector) {
        this.hazelcastInstance = HazelcastConfig.initializeHazelcast();
        this.diskInvertedIndexRepository = dataTypeSelector.getIndexRepository();
        this.diskMetadataRepository = dataTypeSelector.getMetadataRepository();
        this.indexRepository = new HazelcastIndexRepository(this.hazelcastInstance);
        this.metadataRepository = new HazelcastMetadataRepository(this.hazelcastInstance);
        this.crawlService = new CrawlService(new GutenbergCrawlerAdapter(), hazelcastInstance);
        StopwordsLoader stopwordsLoader = new StopwordsLoader();
        this.createIndexService = new CreateIndexService(this.indexRepository, stopwordsLoader.getStopwords());
        this.createMetadataService = new CreateMetadataService(this.metadataRepository);

        this.indexedBooks = this.hazelcastInstance.getSet("indexedBooks");
    }

    public void initialize() {
        initializeHazelcastStorage();
        initializeApi();
    }

    public void initializeHazelcastStorage() {
        StopwordsLoader stopwordsLoader = new StopwordsLoader();
        CreateIndexService createIndexService = new CreateIndexService(this.indexRepository, stopwordsLoader.getStopwords());
        CreateMetadataService createMetadataService = new CreateMetadataService(this.metadataRepository);
        initializeDatamart(createIndexService, createMetadataService);
    }

    public void initializeApi() {
        QueryService queryService = new Queries();
        SearchService searchService = new SearchService(queryService, hazelcastInstance, diskInvertedIndexRepository, diskMetadataRepository);
        new SearchController(searchService, hazelcastInstance, diskMetadataRepository, diskInvertedIndexRepository);
        System.out.println("API is running at http://localhost:4567");
    }

    public void initializeDatamart(CreateIndexService createIndexService, CreateMetadataService createMetadataService) {
        try {
            List<Book> datalakeBooks = BooksDatalakeLoader.loadBooksFromDatalake();

            datalakeBooks.forEach(book -> indexedBooks.add(book.getId()));
            createIndexService.createAndUpdateInvertedIndex(datalakeBooks);
            createMetadataService.createAndUpdateMetadata(datalakeBooks);

            System.out.println("Initial inverted index generated and saved to file.");
        } catch (IOException e) {
            System.err.println("Error loading books from datalake: " + e.getMessage());
        }
    }


    public CreateIndexService getCreateIndexService() {
        return createIndexService;
    }

    public CreateMetadataService getCreateMetadataService() {
        return createMetadataService;
    }

    public ISet<String> getIndexedBooks() {
        return indexedBooks;
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public IndexRepository getDiskInvertedIndexRepository() {
        return diskInvertedIndexRepository;
    }

    public MetadataRepository getDiskMetadataRepository() {
        return diskMetadataRepository;
    }

    public IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public MetadataRepository getMetadataRepository() {
        return metadataRepository;
    }

    public CrawlService getCrawlService() {
        return crawlService;
    }
}
