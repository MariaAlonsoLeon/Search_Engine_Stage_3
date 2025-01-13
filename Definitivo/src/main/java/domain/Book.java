package domain;

public class Book {
    private final String id;
    private final String content;

    public Book(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
