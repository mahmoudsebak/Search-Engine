import java.util.ArrayList;
import java.util.HashMap;

public class QueryProcessor {
    public static final int PagesLimit = 10;
    /**
     * 
     * @param query : search query(sentence)
     * @param page : page number
     * @return  : query result
     */
    public static ArrayList<HashMap<String,String>> ProcessQuery(String query,int page)
    {
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();

        ArrayList<HashMap<String,String>> result = null;

        if (query.startsWith("\"") && query.endsWith("\"")) {
			result = adapter.queryPhrase(query.substring(1, query.length()-1), PagesLimit, page);
		}
		else {
            ArrayList<String> words = WordsExtractionProcess.ApplyingStemming(WordsExtractionProcess.RemovingStoppingWords(WordsExtractionProcess.SplitStrings(query)));
            String [] queryWords = words.toArray(new String[0]);
            result = adapter.queryWords(queryWords, PagesLimit, page);
        }
        // for (HashMap<String,String> hashMap : result) {
        //     String content=hashMap.get("content");
        //     int index=content.indexOf(query, 0);
        // }
        adapter.close();
        return result;
    }
}