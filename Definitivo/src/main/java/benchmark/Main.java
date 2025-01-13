package benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws RunnerException, IOException {
        int warmupIterations = 0;
        int measurementIterations = 1;
        int forks = 1;

        runStoreScalabilityBenchmark(warmupIterations, measurementIterations, forks);
        runQueryBenchmark(warmupIterations, measurementIterations, forks);

        System.out.println("Benchmarks executed and results stored in JSON.");
    }

    private static void runStoreScalabilityBenchmark(int warmupIterations, int measurementIterations, int forks) throws RunnerException {
        System.out.println("Executing benchmark.StoreScalabilityTest...");

        Options options = buildOptions(
                StoreScalabilityTest.class.getSimpleName(),
                "store_scalability_benchmark_results.json",
                warmupIterations,
                measurementIterations,
                forks
        );

        new Runner(options).run();
    }

    private static void runQueryBenchmark(int warmupIterations, int measurementIterations, int forks) throws RunnerException {
        System.out.println("Executing benchmark.QueryBenchmark...");

        Options options = buildOptions(
                QueryScalabilityTest.class.getSimpleName(),
                "query_benchmark_results_modified.json",
                warmupIterations,
                measurementIterations,
                forks
        );

        new Runner(options).run();
    }

    private static Options buildOptions(String benchmarkName, String outputFileName, int warmupIterations, int measurementIterations, int forks) {
        ChainedOptionsBuilder optionsBuilder = new OptionsBuilder()
                .include(benchmarkName)
                .warmupIterations(warmupIterations)
                .measurementIterations(measurementIterations)
                .forks(forks)
                .result(outputFileName)
                .resultFormat(ResultFormatType.JSON);

        return optionsBuilder.build();
    }
}
