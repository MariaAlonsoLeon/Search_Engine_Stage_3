package benchmark;

import adapters.persistence.*;
import domain.*;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StoreScalabilityTest {

    private List<Book> books;
    private List<Metadata> metadataList;

    private IndexRepository fileRepo;
    private IndexRepository cborRepo;
    private IndexRepository avroRepo;
    private IndexRepository kryoRepo;
    private IndexRepository binaryRepo;

    private MetadataRepository fileMetadataRepo;
    private MetadataRepository cborMetadataRepo;
    private MetadataRepository avroMetadataRepo;
    private MetadataRepository kryoMetadataRepo;
    private MetadataRepository binaryMetadataRepo;

    private File tempFile;
    private File cborFile;
    private File avroFile;
    private File kryoFile;
    private File binaryFile;

    private File metadataTempFile;
    private File cborMetadataFile;
    private File avroMetadataFile;
    private File kryoMetadataFile;
    private File binaryMetadataFile;

    @Param({"100", "500", "1000", "5000"})
    private int datasetSize;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        books = DataGenerator.generateBooks(datasetSize, 50);
        metadataList = generateMetadata(books);

        File tempDir = Files.createTempDirectory("benchmark").toFile();
        tempFile = new File(tempDir, "testFileRepo.txt");
        cborFile = new File(tempDir, "testCBORRepo.cbor");
        avroFile = new File(tempDir, "testAvroRepo.avro");
        kryoFile = new File(tempDir, "testKryoRepo.kryo");
        binaryFile = new File(tempDir, "testBinaryRepo.bin");
        metadataTempFile = new File(tempDir, "testFileMetadataRepo.txt");
        cborMetadataFile = new File(tempDir, "testCBORMetadataRepo.cbor");
        avroMetadataFile = new File(tempDir, "testAvroMetadataRepo.avro");
        kryoMetadataFile = new File(tempDir, "testKryoMetadataRepo.kryo");
        binaryMetadataFile = new File(tempDir, "testBinaryMetadataRepo.bin");

        tempFile.createNewFile();
        cborFile.createNewFile();
        avroFile.createNewFile();
        kryoFile.createNewFile();
        binaryFile.createNewFile();
        metadataTempFile.createNewFile();
        cborMetadataFile.createNewFile();
        avroMetadataFile.createNewFile();
        kryoMetadataFile.createNewFile();
        binaryMetadataFile.createNewFile();

        fileRepo = new FileIndexRepository(tempFile.getAbsolutePath());
        cborRepo = new CBORIndexRepository(cborFile.getAbsolutePath());
        avroRepo = new AvroIndexRepository(avroFile.getAbsolutePath());
        kryoRepo = new KryoIndexRepository(kryoFile.getAbsolutePath());
        binaryRepo = new BinaryJavaSerializationIndexRepository(binaryFile.getAbsolutePath());

        fileMetadataRepo = new FileMetadataRepository(metadataTempFile.getAbsolutePath());
        cborMetadataRepo = new CBORMetadataRepository(cborMetadataFile.getAbsolutePath());
        avroMetadataRepo = new AvroMetadataRepository(avroMetadataFile.getAbsolutePath());
        kryoMetadataRepo = new KryoMetadataRepository(kryoMetadataFile.getAbsolutePath());
        binaryMetadataRepo = new BinaryJavaSerializationMetadataRepository(binaryMetadataFile.getAbsolutePath());
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        deleteFile(tempFile);
        deleteFile(cborFile);
        deleteFile(avroFile);
        deleteFile(kryoFile);
        deleteFile(binaryFile);
        deleteFile(metadataTempFile);
        deleteFile(cborMetadataFile);
        deleteFile(avroMetadataFile);
        deleteFile(kryoMetadataFile);
        deleteFile(binaryMetadataFile);
        tempFile.getParentFile().delete();
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    @Benchmark
    public void scalabilityWriteInvertedIndexToFileRepository() {
        fileRepo.saveInvertedIndex(booksToIndex(books));
    }

    @Benchmark
    public void scalabilityWriteMetadataToFileRepository() {
        for (Metadata metadata : metadataList) {
            fileMetadataRepo.saveMetadata(metadata);
        }
    }

    @Benchmark
    public void scalabilityWriteInvertedIndexToCBORRepository() {
        cborRepo.saveInvertedIndex(booksToIndex(books));
    }

    @Benchmark
    public void scalabilityWriteMetadataToCBORRepository() {
        for (Metadata metadata : metadataList) {
            cborMetadataRepo.saveMetadata(metadata);
        }
    }

    @Benchmark
    public void scalabilityWriteInvertedIndexToAvroRepository() {
        avroRepo.saveInvertedIndex(booksToIndex(books));
    }

    @Benchmark
    public void scalabilityWriteMetadataToAvroRepository() {
        for (Metadata metadata : metadataList) {
            avroMetadataRepo.saveMetadata(metadata);
        }
    }

    @Benchmark
    public void scalabilityWriteInvertedIndexToKryoRepository() {
        kryoRepo.saveInvertedIndex(booksToIndex(books));
    }

    @Benchmark
    public void scalabilityWriteMetadataToKryoRepository() {
        for (Metadata metadata : metadataList) {
            kryoMetadataRepo.saveMetadata(metadata);
        }
    }

    @Benchmark
    public void scalabilityWriteInvertedIndexToBinaryRepository() {
        binaryRepo.saveInvertedIndex(booksToIndex(books));
    }


    @Benchmark
    public void scalabilityWriteMetadataToBinaryRepository() {
        for (Metadata metadata : metadataList) {
            binaryMetadataRepo.saveMetadata(metadata);
        }
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

    private static List<Metadata> generateMetadata(List<Book> books) {
        return books.stream()
                .map(book -> new Metadata(
                        book.getId(),
                        "Author_" + book.getId(),
                        "2023-01-01",
                        "EN"))
                .toList();
    }
}
