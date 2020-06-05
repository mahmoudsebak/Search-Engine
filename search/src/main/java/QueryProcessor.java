import java.net.MalformedURLException;
import java.net.URL;
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
        ArrayList<Pair<Integer, HashMap<String, String>>> res = new ArrayList<>();
        for (HashMap<String, String> entry : result) {
            URL url = null;
            try {
                url = new URL(entry.get("url"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String BaseURL = "";
            if(url != null)
                BaseURL = url.getProtocol() + "://" + url.getHost();
            res.add(new Pair<Integer,HashMap<String, String>>(adapter.getUserURLFreq(BaseURL),entry));
        }

        for(int i=0;i<res.size();i++)
        {
            int mx_idx = i;
            for(int j=i+1;j<res.size();j++)
            {
                if(res.get(j).first > res.get(mx_idx).first)
                    mx_idx = j;
            }
            Pair<Integer,HashMap<String, String>> temp = res.get(i);
            res.set(i,res.get(mx_idx));
            res.set(mx_idx, temp);
        }

        int idx = 0;
        for(Pair<Integer,HashMap<String,String>> entry : res)
            result.set(idx++, entry.second);
            
        adapter.close();
        return result;
    }
}