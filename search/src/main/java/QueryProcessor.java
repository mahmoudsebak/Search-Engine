import java.util.ArrayList;
import java.util.HashMap;

public class QueryProcessor {
    public static final int PAGES_LIMIT = 10;
    public static final int CONTENT_LIMIT = 200;
    /**
     * 
     * @param query : search query(sentence)
     * @param page  : page number
     * @return : query result
     */
    public static ArrayList<HashMap<String, String>> ProcessQuery(String query, int page, boolean isImage) {
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();

        ArrayList<HashMap<String, String>> result = null;

        if (query.startsWith("\"") && query.endsWith("\"")) {
            query = query.substring(1, query.length() - 1);
            ArrayList<String> words = WordsExtractionProcess.ApplyingStemming(
            WordsExtractionProcess.RemovingStoppingWords(WordsExtractionProcess.SplitStrings(query)));
            String[] queryWords = words.toArray(new String[0]);
            if (isImage)
                result = adapter.queryImage(query, queryWords, PAGES_LIMIT, page);
            else {
                result = adapter.queryPhrase(query, queryWords, PAGES_LIMIT, page);
                for (HashMap<String, String> hashMap : result) {
                    String content = hashMap.get("content");
                    if (content != null) {
                        int index = content.indexOf(query, 0);
                        if (index != -1) {
                            content = content.substring(index);
                        }
                        content = content.substring(0, Math.min(content.length(), CONTENT_LIMIT)) + "...";
                        hashMap.put("content", content);
                    }
                }
            }
        } else {
            ArrayList<String> words = WordsExtractionProcess.ApplyingStemming(
                    WordsExtractionProcess.RemovingStoppingWords(WordsExtractionProcess.SplitStrings(query)));
            String[] queryWords = words.toArray(new String[0]);
            if (isImage)
                result = adapter.queryImage(queryWords, PAGES_LIMIT, page);
            else {
                result = adapter.queryWords(queryWords, PAGES_LIMIT, page);
                for (HashMap<String, String> hashMap : result) {
                    String content = hashMap.get("content");
                    if (content != null) {
                        int index = -1;
                        for (int i = 0; i < queryWords.length && index == -1; i++) {
                            index = content.indexOf(queryWords[i], 0);
                        }
                        if (index != -1) {
                            content = content.substring(index);
                        }
                        content = content.substring(0, Math.min(content.length(), CONTENT_LIMIT)) + "...";
                        hashMap.put("content", content);
                    }
                }
            }
        }
        adapter.close();
        return result;
    }
}