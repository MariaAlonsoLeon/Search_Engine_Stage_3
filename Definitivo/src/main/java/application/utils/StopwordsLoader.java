package application.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class StopwordsLoader {

    private final Set<String> stopwords = new HashSet<>();

    public Set<String> loadStopwords(String resourceFileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceFileName)) {
            if (inputStream == null) {
                throw new IOException("Resource file not found: " + resourceFileName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stopwords.add(line.trim().toLowerCase());
                }
            }
            return stopwords;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getStopwords() {
        return loadStopwords("stopwords.txt");
    }
}
