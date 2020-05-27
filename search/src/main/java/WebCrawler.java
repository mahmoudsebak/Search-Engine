
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
public class WebCrawler {
    public static void main(String[] args) throws InterruptedException {
        Set<String> seedPages  = new HashSet<String>();
        seedPages.add("https://www.youtube.com/");
        seedPages.add("https://codeforces.com/");
        seedPages.add("https://www.geeksforgeeks.org/");
        seedPages.add("https://ncataggies.com/galleries/cross-country/elon-invite/287");
        seedPages.add("https://en.wikipedia.org/wiki/Football");
        Crawler crawler = new Crawler(seedPages);
        int ThreadNo = Integer.parseInt(args[0]);
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
    private static final int MAX_PAGES = 5000;
    private ConcurrentHashMap pagesVisited;
    private LinkedBlockingQueue<String> pagesToVisit;

    public Crawler(Set<String> seedPages) {
        this.pagesVisited = new ConcurrentHashMap();
        this.pagesToVisit = new LinkedBlockingQueue<String>();
        this.adapter=new IndexerDbAdapter();
        this.adapter.open();
        for (String page : seedPages) {
            this.pagesToVisit.offer(page);
        }
    }

    public int getPagesVisitedLength() {
        return this.pagesVisited.size();
    }

    public boolean crawl() {
        String url = this.pagesToVisit.poll();
        if (url == null) return false; // There is no pages to visit
        try {
            if (! this.robotSafe(new URL(url))) return false;
        } catch (MalformedURLException e) {
            System.out.println("Invalid URL: " + url);
            return false;
        }
        int res = isValid(url);
        if(res == 2)
            this.getLinks(url);
        else if(res == 1)
            return true;
        return false;
    }

    public synchronized int isValid(String url) {
        if (this.pagesVisited.size() == MAX_PAGES) return 1;  // Finsihed
        if (this.pagesVisited.containsKey(url)) return 0;    // Already visited
        this.pagesVisited.put(url, true);
        return 2;
    }

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
        System.out.println("Visited " + this.getPagesVisitedLength() + " page(s)");
        // Add links to the queue
        this.adapter.addURL(url);
        for (Element link : pageLinks) {
            String page = link.absUrl("href");
            this.pagesToVisit.offer(page);
            this.adapter.addLink(url, page);
        }
    }

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
