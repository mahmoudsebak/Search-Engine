import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;


public class IndexerDbTest {
    public static void main(String[] args) throws Exception {
        IndexerDbAdapter dbAdapter = new IndexerDbAdapter();
        dbAdapter.open();
        int id1 = dbAdapter.addURL("https://codeforces.com/");
        //dbAdapter.updateURL("https://codeforces.com/", "a very long document related to competitive programming", "codeforces", .05, .001);
        int id2 = dbAdapter.addURL("https://www.geeksforgeeks.org/");
        dbAdapter.addURL("https://www.youtube.com/");
        //dbAdapter.updateURL("https://www.youtube.com/", null, "youtube", 0.2, 0.3);
        dbAdapter.addLink(id2, id1);
        dbAdapter.addWord("programming", id2, 0.4);
        dbAdapter.addWord("programming", id1, 0.4);
        dbAdapter.addWord("competitive", id1, 0.5);
        dbAdapter.addWord("competitive", id2, 0.5);
        // dbAdapter.addImage(id2, "https://www.geeksforgeeks.org/", "alt");
        // dbAdapter.addImage(id2, "https://codeforces.com/", "alt");
        // dbAdapter.addImage(id2, "https://www.youtube.com/", "alt");
        // dbAdapter.addImage(id1, "https://www.geeksforgeeks.org/", "alt");
        // dbAdapter.addImage(id1, "https://www.youtube.com/", "alt");
        dbAdapter.addTrend("Donald Trump", "United States");
        dbAdapter.addTrend("Recep Tayyip Erdogan", "Turkey");
        dbAdapter.addTrend("Mohamed Salman", "Saudi Arabia");
        dbAdapter.addTrend("Mohamed Salah", "Egypt");

        dbAdapter.resetPagesRank();
        ArrayList<HashMap<String, String>> arr = dbAdapter.queryWords(new String [] {"programming"}, 10, 1);
        Gson gson = new Gson();
        System.out.println(gson.toJson(arr));
        System.out.println();

        arr = dbAdapter.queryWords(new String [] {"competitive"}, 10, 1);
        System.out.println(arr);
        System.out.println();

        arr = dbAdapter.queryImage("programming", 10, 1);
        System.out.println(arr);
        System.out.println();

        arr = dbAdapter.queryImage(new String [] {"competitive"}, 10, 1);
        System.out.println(arr);
        System.out.println();

        arr = dbAdapter.queryPhrase("competitive programming", new String[]{"Competit", "program"}, 10, 1);
        System.out.println(arr);
        System.out.println();

        System.out.println(dbAdapter.getUnindexedURL());
        System.out.println();

        System.out.println(dbAdapter.getUnCrawledURLs());
        System.out.println();

        dbAdapter.crawlURL("https://codeforces.com/");
        System.out.println(dbAdapter.getUnCrawledURLs());
        System.out.println();

        System.out.println(dbAdapter.getCrawledURLs());
        System.out.println();

        System.out.println(dbAdapter.getURLsToBeRecrawled());
        System.out.println();

        System.out.println(dbAdapter.removeDuplicateLinks());
        System.out.println(dbAdapter.removeDuplicateImages());
        System.out.println(dbAdapter.removeDuplicateImageWords());
        System.out.println(dbAdapter.removeDuplicateWords());
        dbAdapter.close();
    }
}