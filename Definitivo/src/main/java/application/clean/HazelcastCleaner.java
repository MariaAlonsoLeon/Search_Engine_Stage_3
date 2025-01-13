package application.clean;

import application.tasks.TaskExecutor;
import com.hazelcast.collection.IList;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.map.IMap;
import domain.*;

import java.util.concurrent.*;

public class HazelcastCleaner {

    private final HazelcastInstance hazelcastInstance;
    private final String datalakePath;
    private final IndexRepository indexRepository;
    private final MetadataRepository metadataRepository;
    private final int cleanThreshold;
    private final String nodeId;
    private final ExecutorService executorService;
    private final Object monitor = new Object();

    public HazelcastCleaner(HazelcastInstance hazelcastInstance,
                            String datalakePath,
                            IndexRepository indexRepository,
                            MetadataRepository metadataRepository,
                            int cleanThreshold) {
        this.hazelcastInstance = hazelcastInstance;
        this.datalakePath = datalakePath;
        this.indexRepository = indexRepository;
        this.metadataRepository = metadataRepository;
        this.cleanThreshold = cleanThreshold;
        this.nodeId = hazelcastInstance.getCluster().getLocalMember().getUuid().toString();
        this.executorService = Executors.newFixedThreadPool(3);
    }

    public void startMonitoringTask(TaskExecutor taskExecutor) {
        taskExecutor.submitTask(3, () -> {
            FencedLock lock = hazelcastInstance.getCPSubsystem().getLock("datalakeLock");
            lock.lock();
            try {
                monitorDatalakeSize();
            } finally {
                lock.unlock();
            }
        });

    }

    private void monitorDatalakeSize() {
        IMap<String, String> datalakeMap = hazelcastInstance.getMap("datalake");

        if (datalakeMap.size() >= cleanThreshold) {
            synchronized (monitor) {
                executorService.submit(() -> saveAndClean(datalakeMap));
            }
        }
    }

    private void saveAndClean(IMap<String, String> datalakeMap) {
        System.out.println("Threshold reached. Starting save and clean process...");

        IMap<String, String> tempMap = hazelcastInstance.getMap("datalakeTemp");
        datalakeMap.forEach((key, content) -> {
            if (key.startsWith(nodeId + ":")) {
                tempMap.put(key, content);
            }
        });

        saveToDisk();

        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock("datalakeLock");
        lock.lock();
        try {
            tempMap.forEach((key, content) -> datalakeMap.put(key, content));
            tempMap.clear();
        } finally {
            lock.unlock();
        }

        System.out.println("Save and clean process completed.");
    }

    private void saveToDisk() {
        System.out.println("Saving datalake to disk...");

        IMap<String, InvertedIndex> invertedIndexMap = hazelcastInstance.getMap("invertedIndex");
        InvertedIndex invertedIndex = invertedIndexMap.get("globalIndex");
        if (invertedIndex != null) {
            indexRepository.saveInvertedIndex(invertedIndex);
            System.out.println("Inverted index saved to disk.");
        }

        IList<Metadata> metadataList = hazelcastInstance.getList("metadata");
        if (metadataList != null) {
            metadataList.forEach(metadataRepository::saveMetadata);
            System.out.println("Metadata saved to disk.");
        }

        System.out.println("Datalake, Inverted Index, and Metadata saved to disk.");
    }
}
