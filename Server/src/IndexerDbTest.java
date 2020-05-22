import java.util.ArrayList;

public class IndexerDbTest {
    public static void main(String[] args) throws Exception {
        IndexerDbAdapter dbAdapter = new IndexerDbAdapter();
        dbAdapter.open();
        dbAdapter.addURL("https://codeforces.com/", "a very long document related to competitive programming");
        dbAdapter.addURL("https://www.geeksforgeeks.org/", "a very long document related to programming in general");
        dbAdapter.addLink("https://www.geeksforgeeks.org/", "https://codeforces.com/");
        dbAdapter.addWord("programming", "https://www.geeksforgeeks.org/", 0.4);
        dbAdapter.addWord("programming", "https://codeforces.com/", 0.4);
        dbAdapter.addWord("competitive", "https://codeforces.com/", 0.5);

        dbAdapter.resetPagesRank();
        ArrayList<String> arr = dbAdapter.queryWords(new String [] {"programming"}, 10, 1);
        for (String string : arr) {
            System.out.println(string);
        }
        System.out.println();
        arr = dbAdapter.queryWords(new String [] {"competitive"}, 10, 1);
        for (String string : arr) {
            System.out.println(string);
        }
        System.out.println();
        arr = dbAdapter.queryPhrase("competitive programming", 10, 1);
        for (String string : arr) {
            System.out.println(string);
        }
        dbAdapter.close();
    }
}