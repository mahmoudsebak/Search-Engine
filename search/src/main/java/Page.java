import java.util.HashMap;

public class Page {
    private String url, content, title;
    private Double dateScore, geoScore;
    private HashMap<String, Double> words;
    private String[] images;

    public Page(String url, String content, String title, Double dateScore, Double geoScore,
            HashMap<String, Double> words, String[] images) {
        this.url = url;
        this.content = content;
        this.title = title;
        this.dateScore = dateScore;
        this.geoScore = geoScore;
    }

    public String getContent() {
        return content;
    }

    public Double getDateScore() {
        return dateScore;
    }

    public Double getGeoScore() {
        return geoScore;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String[] getImages() {
        return images;
    }

    public HashMap<String, Double> getWords() {
        return words;
    }
}