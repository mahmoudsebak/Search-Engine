
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * This class is used to crawl web pages
 **/
public class WebCrawler {
    public static void main(String[] args) throws InterruptedException {
        Set<String> seedPages  = new HashSet<String>();
        // seedPages.add("https://codeforces.com/");
        // seedPages.add("https://www.geeksforgeeks.org/");
        seedPages.add("http://odp.org/");
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();
        ArrayList<String> visited = adapter.getCrawledURLs();
        ArrayList<String> toVisit = adapter.getUnCrawledURLs(); 
        for (String page : seedPages) 
            toVisit.add(page);
        Crawler crawler = new Crawler(toVisit, visited, adapter);
        int ThreadNo = Integer.parseInt(args[0]);
        // int ThreadNo = 1;
        System.out.println(ThreadNo);
        Thread [] t = new Thread [ThreadNo];
        for(int i = 0; i < ThreadNo; i++) t[i] = new Thread(new CrawlerRunnable(crawler));
        long start = System.currentTimeMillis();
        for(int i = 0; i < ThreadNo; i++) t[i].start();
        for(int i = 0; i < ThreadNo; i++) t[i].join();
        long time = System.currentTimeMillis() - start;
        System.out.printf("Time taken = " + time + " ms\n\n");
        System.out.println("\n**Done** Visited " + crawler.getPagesVisitedLength() + " web page(s)");

    }
}

/**
 * This class is used to recrawl previously crawled pages depending on the date score calculated by the ranker
 **/
class Recrawler {
    public static void main(String[] args) throws InterruptedException {
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();
        ArrayList<String> toBeRecrawled = adapter.getURLsToBeRecrawled();
        Crawler crawler = new Crawler(toBeRecrawled, null, adapter);
        int ThreadNo = Integer.parseInt(args[0]);
        System.out.println(ThreadNo);
        Thread [] t = new Thread [ThreadNo];
        for(int i = 0; i < ThreadNo; i++) t[i] = new Thread(new CrawlerRunnable(crawler));
        long start = System.currentTimeMillis();
        for(int i = 0; i < ThreadNo; i++) t[i].start();
        for(int i = 0; i < ThreadNo; i++) t[i].join();
        long time = System.currentTimeMillis() - start;
        System.out.printf("Time taken = " + time + " ms\n\n");
        System.out.println("\n**Done** Recrawled " + crawler.getPagesVisitedLength() + " web page(s)");

    }
}
class CrawlerRunnable implements Runnable {
    private Crawler crawler;

    public CrawlerRunnable (Crawler crawler) {
        this.crawler = crawler;
    }

    public void run () {
        boolean finish = false;
        while(!finish) {
            finish = crawler.crawl();
        }
    }
}

class Crawler {
    private IndexerDbAdapter adapter;
    private ConcurrentHashMap <String, Boolean> pagesVisited;
    private LinkedBlockingQueue<String> pagesToVisit;
    private static final int MAX_PAGES = 5000;

    public Crawler(ArrayList<String> toVisit, ArrayList<String> visited, IndexerDbAdapter adapter) {
        this.pagesVisited = new ConcurrentHashMap<String, Boolean>();
        this.pagesToVisit = new LinkedBlockingQueue<String>();
        this.adapter = adapter;
        for (String page : toVisit) {
            this.pagesToVisit.offer(page);
        }
        for (String page : visited) {
            this.pagesVisited.put(page, true);
        }
    }
    /**
     * 
     * @return number of crawled pages
     */
    public int getPagesVisitedLength() {
        return this.pagesVisited.size();
    }
    /**
     * This function is used to crawl a certain url following robot rules
	 *
     * @return true if the crawler has crawled max number of page, false otherwise
     */
    public boolean crawl() {
        String url = this.pagesToVisit.poll();
        if (url == null) return false; // There is no pages to visit
        try {
            if (! this.robotSafe(new URL(url))) return false;
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL: " + url);
            return false;
        }
        int res = visitURL(url);
        if(res == 2)
            this.getLinks(url);
        else if(res == 1)
            return true;
        return false;
    }
    /**
     * This function is used to visit a url
	 *
	 * @param url: the url to be visited
     * @return 0 if the url is crawled before, 1 if the max number of urls are added in database and 2 if it is valid to visit the url
     */
    public synchronized int visitURL(String url) {
        if (this.pagesVisited.size() + this.pagesVisited.size() == MAX_PAGES) return 1;  // Finsihed
        if (this.pagesVisited.containsKey(url)) return 0;    // Already visited
        this.pagesVisited.put(url, true);
        this.adapter.addURL(url);
        this.adapter.crawlURL(url);
        System.out.println("Visited " + this.getPagesVisitedLength() + " page(s)");
        return 2;
    }
    /**
     * This function is used to extract link from a url and add them to the database and add links between src and dst urls
	 *
	 * @param url: the url to extract links from
     */
    public void getLinks(String url) {
        Document htmlPage = null;
        try {
            htmlPage = Jsoup.connect(url).get();
        } catch (IOException e) {
            return;
        }
        // Extract Links from web page
        Elements pageLinks = htmlPage.select("a[href]");
        System.out.println("Thread " + Thread.currentThread().getId() + " visited page: "
                + url + " \nFound (" + pageLinks.size() + ") link(s)");

        // Add links to the queue
        for (Element link : pageLinks) {
            String page = link.absUrl("href");
            this.pagesToVisit.offer(page);
            this.adapter.addURL(page);
            this.adapter.addLink(url, page);
        }
    }
    /**
     * This function is used to check for robot rules in robots.txt file
	 *
	 * @param url: the url to be visited
	 * @return true if the url is robot safe, false otherwise
     */
    public boolean robotSafe(URL url)
    {
        String robot = url.getProtocol() + "://" + url.getHost() + "/robots.txt";
        URL robotUrl;
        try { robotUrl = new URL(robot);
        } catch (MalformedURLException e) {
            return false;
        }

        String strCommands = null;
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(robotUrl.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
            {
                if(strCommands == null)
                    strCommands = inputLine;
                else
                    strCommands += inputLine;
                strCommands += "\n";
            }
            in.close();
            if(strCommands == null)
                return true;

        }
        catch (IOException e)
        {
            return true; //no robots.txt file
        }

        if (strCommands.contains("Disallow:"))
        {
            String[] split = strCommands.split("\n");
            ArrayList<RobotRule> robotRules = new ArrayList<>();
            String userAgent = null;
            for (int i = 0; i < split.length; i++)
            {
                String line = split[i].trim();
                if (line.toLowerCase().startsWith("user-agent"))
                {
                    int start = line.indexOf(":") + 1;
                    int end   = line.length();
                    userAgent = line.substring(start, end).trim();
                }
                else if (line.startsWith("Disallow:")) {
                    if (userAgent != null) {
                        int start = line.indexOf(":") + 1;
                        int end   = line.length();
                        RobotRule r = new RobotRule();
                        r.userAgent = userAgent;
                        r.rule = line.substring(start, end).trim();
                        robotRules.add(r);
                    }
                }
            }

            for (RobotRule robotRule : robotRules)
            {
                if (robotRule.rule.length() == 0) continue;         // allows all
                if (robotRule.rule.equals("/")) return false;       // disallows all
                String path = url.getPath();
                if(path.length() >= robotRule.rule.length())
                {
                    String ruleCompare = path.substring(0, robotRule.rule.length());
                    if (ruleCompare.equals(robotRule.rule)) return false;
                }
            }
        }
        return true;
    }
    public void writeToFile(String filename) {
        FileWriter writer;
        try {
            writer = new FileWriter(filename);

            for(Object page : this.pagesVisited.keySet()) {
                try {
                    writer.write("\n" + page);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

class RobotRule
{
    public String userAgent;
    public String rule;
}
