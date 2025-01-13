package main;

import application.services.CrawlService;
import application.update.DatamartUpdater;
import application.initialize.Initializer;
import application.utils.DataTypeSelector;
import application.clean.HazelcastCleaner;
import application.initialize.InitializeUI;
import application.tasks.TaskExecutor;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        InitializeUI uiInitializer = new InitializeUI();
        uiInitializer.initialize();

        TaskExecutor taskExecutor = new TaskExecutor();
        DataTypeSelector dataTypeSelector = new DataTypeSelector("avro");
        Initializer initializer = new Initializer(dataTypeSelector);
        initializer.initialize();
        CrawlService crawlService = initializer.getCrawlService();
        crawlService.startCrawling(4, taskExecutor);

        DatamartUpdater datamartUpdater = new DatamartUpdater(
                initializer.getIndexedBooks(),
                initializer.getHazelcastInstance(),
                initializer.getCreateIndexService(),
                initializer.getCreateMetadataService()
        );
        datamartUpdater.updateDatamarts(taskExecutor);

        HazelcastCleaner cleaner = new HazelcastCleaner(initializer.getHazelcastInstance(),
                "datalake",
                initializer.getDiskInvertedIndexRepository(),
                initializer.getDiskMetadataRepository(),
                400);
        cleaner.startMonitoringTask(taskExecutor);

    }
}