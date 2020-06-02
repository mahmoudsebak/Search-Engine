import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class test {
    public static void main(String[] args) throws IOException {
        String url = "https://www.geeksforgeeks.org/";
        Connection conn = Jsoup.connect(url);
        System.out.println(conn.execute().header("Last-Modified"));
        System.err.println(LastModified(url));
    }
    static String LastModified(String webSiteUrl) throws IOException {
        URL url = new URL(webSiteUrl);
        URLConnection connection = url.openConnection();
        String date=connection.getHeaderField("Last-Modified");
        ArrayList<String>dateArrayList=new ArrayList<>();
        if(date==null)
            return "1 jan 1990";
        else{
            dateArrayList = WordsExtractionProcess.SplitStrings(date);
            return dateArrayList.get(1)+dateArrayList.get(2)+dateArrayList.get(3);
        }
    }
}