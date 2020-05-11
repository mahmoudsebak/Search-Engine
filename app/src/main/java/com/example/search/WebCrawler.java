import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class WebCrawler {
    public static void main(String[] args) throws InterruptedException
    {
        Set<String> seedPages  = new HashSet<String>();
        seedPages.add("https://www.google.com/");
        seedPages.add("https://www.youtube.com/");
        seedPages.add("https://www.geeksforgeeks.org/");
        seedPages.add("https://codeforces.com/");
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
        crawler.writeToFile("webPages.txt");
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
    private static final int MAX_PAGES = 100;
    private HashSet<String> pagesVisited;
    private Queue<String> pagesToVisit;

    public Crawler(Set<String>seedPages)
    {
        this.pagesVisited = new HashSet<String>();
        this.pagesToVisit = new LinkedList<String>();
        for(String page : seedPages)
        {
            this.pagesToVisit.add(page);
        }
    }

    public int getPagesVisitedLength()
    {
        return this.pagesVisited.size();
    }

    private synchronized String getNextPage()
    {
        String nextPage;
        do
        {
            nextPage = this.pagesToVisit.poll();
        } while(this.pagesVisited.contains(nextPage));
        return nextPage;

    }

    public boolean crawl()
    {
        synchronized(pagesVisited)
        {
            if(this.pagesVisited.size() == MAX_PAGES) return true;
            if(this.pagesToVisit.size() == 0) return true;
            String currentUrl = this.getNextPage();
            if(currentUrl == null) return false;
            this.getLinks(currentUrl);
            return false;

        }
    }

    public synchronized void getLinks(String url)
    {
        Document htmlPage = null;
        try {
            if(this.robotSafe(new URL(url)))
                htmlPage = Jsoup.connect(url).get();
            else
                return;

        } catch (IllegalArgumentException  e) {
            System.out.println("Invalid URL: " + url);
            this.pagesVisited.remove(url);
            return;
        }catch (IOException  e) {
            return;
        }
        Elements pageLinks = htmlPage.select("a[href]");
        System.out.println("Thread " + Thread.currentThread().getId() + " visited page: "
                + url + " \nFound (" + pageLinks.size() + ") link(s)");
        System.out.println("Visited " + this.getPagesVisitedLength() + " page(s)");
        this.pagesVisited.add(url);
        for(Element link : pageLinks)
        {
            this.pagesToVisit.add(link.absUrl("href"));
        }
        return;
    }

    public boolean robotSafe(URL url)
    {
        String strRobot = url.getProtocol() + "://" + url.getHost() + "/robots.txt";
        URL urlRobot;
        try { urlRobot = new URL(strRobot);
        } catch (MalformedURLException e) {
            return false;
        }

        String strCommands = null;
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(urlRobot.openStream()));
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
            String mostRecentUserAgent = null;
            for (int i = 0; i < split.length; i++)
            {
                String line = split[i].trim();
                if (line.toLowerCase().startsWith("user-agent"))
                {
                    int start = line.indexOf(":") + 1;
                    int end   = line.length();
                    mostRecentUserAgent = line.substring(start, end).trim();
                }
                else if (line.startsWith("Disallow:")) {
                    if (mostRecentUserAgent != null) {
                        RobotRule r = new RobotRule();
                        r.userAgent = mostRecentUserAgent;
                        int start = line.indexOf(":") + 1;
                        int end   = line.length();
                        r.rule = line.substring(start, end).trim();
                        robotRules.add(r);
                    }
                }
            }

            for (RobotRule robotRule : robotRules)
            {
                String path = url.getPath();
                if (robotRule.rule.length() == 0) return true; // allows everything
                if (robotRule.rule == "/") return false;       // allows nothing

                if (robotRule.rule.length() <= path.length())
                {
                    String pathCompare = path.substring(0, robotRule.rule.length());
                    if (pathCompare.equals(robotRule.rule)) return false;
                }
            }
        }
        return true;
    }
    public void writeToFile(String filename) {
        FileWriter writer;
        try {
            writer = new FileWriter(filename);
            for(String page : this.pagesVisited) {
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
