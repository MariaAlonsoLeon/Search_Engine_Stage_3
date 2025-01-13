package domain;

import java.io.Serializable;

public class Metadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookId;
    private String author;
    private String date;
    private String language;

    public Metadata() {
    }

    public Metadata(String bookId, String author, String date, String language) {
        this.bookId = bookId;
        this.author = author;
        this.date = date;
        this.language = language;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "bookId='" + bookId + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
