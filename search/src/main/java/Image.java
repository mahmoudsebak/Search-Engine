import java.util.ArrayList;

public class Image {
    String src, alt;
    ArrayList<String>words;

    public Image(String src, String alt, ArrayList<String> words) {
        this.src = src;
        this.alt = alt;
        this.words = words;
    }
    
    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public ArrayList<String> getWords() {
        return words;
    }

    public void setWords(ArrayList<String> words) {
        this.words = words;
    }
}