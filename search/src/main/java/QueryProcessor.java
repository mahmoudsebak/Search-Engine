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
    public static ArrayList<HashMap<String,String>> ProcessQuery(String query, int page, boolean isImage)
    {
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();

        ArrayList<HashMap<String,String>> result = null;

        if (query.startsWith("\"") && query.endsWith("\"")) {
            if(isImage)
                result = adapter.queryImage(query.substring(1, query.length()-1), PagesLimit, page);
            else
			    result = adapter.queryPhrase(query.substring(1, query.length()-1), PagesLimit, page);
		}
		else {
            ArrayList<String> words = WordsExtractionProcess.ApplyingStemming(WordsExtractionProcess.RemovingStoppingWords(WordsExtractionProcess.SplitStrings(query)));
            String [] queryWords = words.toArray(new String[0]);
            if(isImage)
                result = adapter.queryImage(queryWords, PagesLimit, page);
            else
                result = adapter.queryWords(queryWords, PagesLimit, page);
        }
         for (HashMap<String,String> hashMap : result) {
             String content=hashMap.get("content");
             int index=content.indexOf(query, 0);
             if(index!=-1){
                content=content.substring(index, content.length());
                hashMap.put("content",content);
             }else{
                hashMap.put("content",content);
             }
         }
        adapter.close();
        return result;
    }
}