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
    	Crawler crawler = new Crawler(seedPages);
		int ThreadNo = Integer.parseInt(args[0]);
		System.out.println(ThreadNo);
		crawler.crawl();
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
            String currentUrl = this.getNextPage();
            this.pagesVisited.add(currentUrl);
            if(currentUrl == null) return false;
            this.getLinks(currentUrl);
            return false;
            
    	}
    }
    
    public synchronized void getLinks(String url)
    {
    	Document htmlPage = null;
		try {
			htmlPage = Jsoup.connect(url).get();
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
        for(Element link : pageLinks)
        {
        	this.pagesToVisit.add(link.absUrl("href"));
        }
        return;
    }

    public void writeToFile(String filename) {
        FileWriter writer;
        try {
            writer = new FileWriter(filename);
            this.pagesVisited.forEach(a -> {
                try {
                    writer.write("\n" + a);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

