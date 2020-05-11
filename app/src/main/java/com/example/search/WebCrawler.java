import java.io.IOException;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    private static final int MAX_PAGES = 5000;
    private Set<String> pagesVisited = new HashSet<String>();
    private List<String> pagesToVisit = new LinkedList<String>();
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    
    public String getNextPage()
    {
    	String nextPage;
    	do
    	{
    		nextPage = pagesToVisit.remove(0);
    	}while(this.pagesVisited.contains(nextPage));
    	this.pagesVisited.add(nextPage);
    	return nextPage;
    }
    public List<String> getPageLinks(String url)
    {
		try 
		{
	        Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
	        Document htmlDocument =  connection.get();

            if(connection.response().statusCode() == 200)
			{
            	System.out.println("\nVisiting web page: " + url);
			}
			if(!connection.response().contentType().contains("text/html"))
			{
				System.out.println("\nFalied to visit web page");
			}
            Elements pageLinks = htmlDocument.select("a[href]");
            System.out.println("Found " + pageLinks.size() + " links");
            List<String> links = new LinkedList<String>();
            for(Element link : pageLinks)
            {
            	links.add(link.absUrl("href"));
            }
            return links;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}    	
    }
    public List<String> crawl(List<String> seedPages)
    {
    	this.pagesToVisit = seedPages;
        while(this.pagesVisited.size() < MAX_PAGES)
        {
            String currentUrl =  this.getNextPage();
            this.pagesToVisit.addAll(this.getPageLinks(currentUrl));
        }
        return this.pagesToVisit;
    }
    public static void main(String[] args)
    {
    	List<String> seedPages = new LinkedList<String>();
    	seedPages.add("https://www.nzfootball.co.nz/");
    	seedPages.add("https://en.wikipedia.org/wiki/Association_football_in_New_Zealand");
    	WebCrawler crawler = new WebCrawler();
    	List<String> webPages = crawler.crawl(seedPages);
    	
    	for(String page : webPages)
    	{
    		System.out.println(page);
    	}
    	
    }
    
}
