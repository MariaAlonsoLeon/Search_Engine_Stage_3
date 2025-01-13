package application.services;

import domain.Book;
import domain.Metadata;
import domain.MetadataRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateMetadataService {

    private final MetadataRepository metadataRepository;

    public CreateMetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public void createAndUpdateMetadata(List<Book> books) {
        for (Book book: books) {
            String content = book.getContent();

            String author = extractMetadata(content, Pattern.compile("^Author:\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
            String date = extractAndFormatDate(content, Pattern.compile("^Release date:\\s*([A-Za-z]+ \\d{1,2}, \\d{4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
            String language = extractMetadata(content, Pattern.compile("^Language:\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));

            Metadata metadata = new Metadata(book.getId(), author, date, language);
            metadataRepository.saveMetadata(metadata);
        }
    }

    private String extractMetadata(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "Unknown";
    }

    private String extractAndFormatDate(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String dateStr = matcher.group(1).trim();
            try {
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                return outputDateFormat.format(inputDateFormat.parse(dateStr));
            } catch (ParseException e) {
                e.printStackTrace();
                return "Unknown";
            }
        }
        return "Unknown";
    }
}
