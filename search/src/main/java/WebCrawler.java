
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
        seedPages.add("https://en.wikipedia.org/wiki/Main_Page");
        seedPages.add("https://www.geeksforgeeks.org/");
        seedPages.add("https://www.imdb.com/");
        seedPages.add("https://www.spotify.com/eg-en/");
        seedPages.add("https://edition.cnn.com/");
        seedPages.add("https://www.gamespot.com/");
        seedPages.add("https://www.skysports.com/");
        seedPages.add("https://cooking.nytimes.com/");
        seedPages.add("https://en.unesco.org/");
        seedPages.add("https://www.who.int/");

        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();
        ArrayList<String> visited = adapter.getCrawledURLs();
        ArrayList<String> toVisit = adapter.getUnCrawledURLs(); 
        for (String page : seedPages) 
            toVisit.add(page);
        int ThreadNo = Integer.parseInt(args[0]);
        Crawler crawler = new Crawler(toVisit, visited, adapter, false);
        Thread [] t = new Thread [ThreadNo];
        for(int i = 0; i < ThreadNo; i++) t[i] = new Thread(new CrawlerRunnable(crawler));
        long start = System.currentTimeMillis();
        for(int i = 0; i < ThreadNo; i++) t[i].start();
        for(int i = 0; i < ThreadNo; i++) t[i].join();
        long time = System.currentTimeMillis() - start;
        System.out.println("\nTime taken = " + time + " ms");
        System.out.println("Crawled = " + crawler.getPagesVisitedLength() + " web page(s)");
        System.out.println("ToBeCrawled = " + crawler.getPagesToVisitLength() + " web page(s)");

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
        Crawler crawler = new Crawler(toBeRecrawled, null, adapter, true);
        int ThreadNo = Integer.parseInt(args[0]);
        Thread [] t = new Thread [ThreadNo];
        for(int i = 0; i < ThreadNo; i++) t[i] = new Thread(new CrawlerRunnable(crawler));
        long start = System.currentTimeMillis();
        for(int i = 0; i < ThreadNo; i++) t[i].start();
        for(int i = 0; i < ThreadNo; i++) t[i].join();
        long time = System.currentTimeMillis() - start;
        System.out.println("\nTime taken = " + time + " ms");
        System.out.println("Crawled = " + crawler.getPagesVisitedLength() + " web page(s)");
        System.out.println("ToBeCrawled = " + crawler.getPagesToVisitLength() + " web page(s)");
    }
}

/**
 * This class is used in threading
 **/
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
    private Boolean isRecraler;
    private static final int MAX_PAGES_TO_BE_CRAWLED = 5000;
    private static final int MAX_PAGES_TO_BE_RECRAWLED = 10;

    public Crawler(ArrayList<String> toVisit, ArrayList<String> visited, IndexerDbAdapter adapter, Boolean isRecrawler) {
        this.pagesVisited = new ConcurrentHashMap<String, Boolean>();
        this.pagesToVisit = new LinkedBlockingQueue<String>();
        this.isRecraler = isRecrawler;
        this.adapter = adapter;
        if(toVisit != null)
        {
            for (String page : toVisit) {
                this.pagesToVisit.offer(page);
            }
        }
        if(visited != null)
        {
            for (String page : visited) {
                this.pagesVisited.put(page, true);
            }
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
     * 
     * @return number of pages to be visited
     */
    public int getPagesToVisitLength() {
        return this.pagesToVisit.size();
    }

    /**
     * This function is used to crawl a certain url following robot rules
	 *
     * @return true if the crawler has crawled max number of pages, false otherwise
     */
    public boolean crawl() {
        String url = this.pagesToVisit.poll();
        if (url == null) return true; // There is no pages to visit
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
        if (!this.isRecraler && this.pagesVisited.size() + this.pagesToVisit.size() >= MAX_PAGES_TO_BE_CRAWLED) return 1;
        if (this.isRecraler && this.pagesVisited.size() == MAX_PAGES_TO_BE_RECRAWLED) return 1;  
        if (this.pagesVisited.containsKey(url)) return 0;    // Already visited
        try {
            url = this.normalizeUrl(url);
        } catch (URISyntaxException e) {
            return 0;
        }
        this.pagesVisited.put(url, true);
        this.adapter.addURL(url);
        this.adapter.crawlURL(url);
        System.out.println("Visited " + this.getPagesVisitedLength() + " page(s)");
        System.out.println("To Visit " + this.getPagesToVisitLength() + " page(s)");
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
            try {
                page = this.normalizeUrl(page);
            } catch (URISyntaxException e) {
                continue;
            }
            this.pagesToVisit.offer(page);
            this.adapter.addURL(page);
            this.adapter.addLink(url, page);
        }
        
    }

    /**
     * This function is used to extract link
	 *
	 * @param url: the url to be normalized
     * @return a normalized url
     */
    public String normalizeUrl(String url) throws URISyntaxException {
        if (url == null) {
            return null;
        }
        if(url.indexOf('?') != -1)
            url = url.substring(0, url.indexOf('?'));
        if (url.indexOf('#') != -1)
            url = url.substring(0, url.indexOf('#'));
        URI uri = new URI(url);
        if (!uri.isAbsolute()) {
            throw new URISyntaxException(url, "Not an absolute URL");
        }
        uri = uri.normalize();
        String path = uri.getPath();
        if (path != null) {
            path = path.replaceAll("//*/", "/");
            if (path.length() > 0 && path.charAt(path.length() - 1) == '/')
                path = path.substring(0, path.length() - 1);
        }
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                path, uri.getQuery(), uri.getFragment()).toString();
    }

    /**
     * This function is used to check for robot rules in robots.txt file
	 *
	 * @param url: the url to be visited
	 * @return true if the url is robot safe, false otherwise
     */
    public boolean robotSafe(URL url) {
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
                    userAgent = line.substring(start, end).trim().toLowerCase();
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
                // disallow when user agent is googlebot or *
                if (robotRule.userAgent.equals("googlebot") || robotRule.userAgent.equals("*")) {
                    if (robotRule.rule.equals("/"))
                        return false; // disallows all
                    String path = url.getPath();
                    if (path.length() >= robotRule.rule.length()) {
                        String ruleCompare = path.substring(0, robotRule.rule.length());
                        if (ruleCompare.equals(robotRule.rule))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * This function is write the crawled pages ina file
	 *
	 * @param filename: name of the file to write in
     */
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

class RobotRule {
    public String userAgent;
    public String rule;
}
