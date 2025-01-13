package benchmark;

import adapters.persistence.*;
import adapters.queries.SimpleQuery;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import domain.*;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class QueryScalabilityTest {

    private HazelcastInstance hazelcastInstance;

    private IndexRepository fileRepo;
    private IndexRepository cborRepo;
    private IndexRepository hazelcastRepo;
    private IndexRepository avroRepo;
    private IndexRepository kryoRepo;
    private IndexRepository binaryRepo;

    private List<Book> books;
    private InvertedIndex invertedIndex;

    private File tempFile;
    private File cborFile;
    private File avroFile;
    private File kryoFile;
    private File binaryFile;

    @Param({"100", "500", "1000", "5000"})
    private int datasetSize;

    private String queryWord;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        hazelcastInstance = Hazelcast.newHazelcastInstance();

        books = DataGenerator.generateBooks(datasetSize, 50);
        invertedIndex = booksToIndex(books);

        queryWord = books.get(0).getContent().split("\\s+")[0].toLowerCase();

        tempFile = Files.createTempFile("testFileRepo", ".txt").toFile();
        cborFile = Files.createTempFile("testCBORRepo", ".cbor").toFile();
        avroFile = Files.createTempFile("testAvroRepo", ".avro").toFile();
        kryoFile = Files.createTempFile("testKryoRepo", ".kryo").toFile();
        binaryFile = Files.createTempFile("testBinaryRepo", ".bin").toFile();

        fileRepo = new FileIndexRepository(tempFile.getAbsolutePath());
        cborRepo = new CBORIndexRepository(cborFile.getAbsolutePath());
        hazelcastRepo = new HazelcastIndexRepository(hazelcastInstance);
        avroRepo = new AvroIndexRepository(avroFile.getAbsolutePath());
        kryoRepo = new KryoIndexRepository(kryoFile.getAbsolutePath());
        binaryRepo = new BinaryJavaSerializationIndexRepository(binaryFile.getAbsolutePath());


        fileRepo.saveInvertedIndex(invertedIndex);
        cborRepo.saveInvertedIndex(invertedIndex);
        hazelcastRepo.saveInvertedIndex(invertedIndex);
        avroRepo.saveInvertedIndex(invertedIndex);
        kryoRepo.saveInvertedIndex(invertedIndex);
        binaryRepo.saveInvertedIndex(invertedIndex);
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
        }
        deleteFile(tempFile);
        deleteFile(cborFile);
        deleteFile(avroFile);
        deleteFile(kryoFile);
        deleteFile(binaryFile);
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    @Benchmark
    public QueryResult queryWithFileRepository() {
        Query query = new SimpleQuery(queryWord, hazelcastInstance, fileRepo);
        return query.execute();
    }

    @Benchmark
    public QueryResult queryWithCBORRepository() {
        Query query = new SimpleQuery(queryWord, hazelcastInstance, cborRepo);
        return query.execute();
    }

    @Benchmark
    public QueryResult queryWithHazelcastRepository() {
        Query query = new SimpleQuery(queryWord, hazelcastInstance, hazelcastRepo);
        return query.execute();
    }

    @Benchmark
    public QueryResult queryWithAvroRepository() {
        Query query = new SimpleQuery(queryWord, hazelcastInstance, avroRepo);
        return query.execute();
    }


    @Benchmark
    public QueryResult queryWithKryoRepository() {
        Query query = new SimpleQuery(queryWord, hazelcastInstance, kryoRepo);
        return query.execute();
    }

    @Benchmark
    public QueryResult queryWithBinaryRepository() {
        Query query = new SimpleQuery(queryWord, hazelcastInstance, binaryRepo);
        return query.execute();
    }

    private static InvertedIndex booksToIndex(List<Book> books) {
        InvertedIndex index = new InvertedIndex();
        for (Book book : books) {
            String[] words = book.getContent().split("\\s+");
            for (int i = 0; i < words.length; i++) {
                index.addWord(words[i], book.getId(), List.of(i));
            }
        }
        return index;
    }
}
