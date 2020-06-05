import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

public class PageRank
{
    public static final int Iterations = 10;
    public static void main(String[] args)
    {
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();

        long startTime = System.nanoTime();
        ArrayList<Pair<String,String>> connections = adapter.fetchAllLinks();
        HashMap<String,Double> ret = CalculatePageRank(connections);
        int i = 0;
        for(HashMap.Entry<String,Double> entry : ret.entrySet())
        {
            adapter.updateURL(entry.getKey(), entry.getValue());
            System.out.println(i++);
        }
        long endTime = System.nanoTime();

        adapter.close();
        System.out.println("Taken time : "+ ((endTime-startTime)*1.0/1e9));
    }
    /**
     *
     * @param connections: list of pairs <src url,dst url>
     * @return map of each url and its pageRank
     */
    public static HashMap<String,Double> CalculatePageRank(ArrayList<Pair<String,String>> connections)
    {
        HashMap<String,Double> PagesRank = new HashMap<String, Double>();
        HashMap<String,Integer> outDegree = new HashMap<String, Integer>();
        HashMap<String,HashSet<String>> Pages = new HashMap<String, HashSet<String>>();

        for(int i = 0 ; i < connections.size() ; i++)
        {
            String src = connections.get(i).first, dst = connections.get(i).second;

            PagesRank.put(src,1.0);
            PagesRank.put(dst,1.0);
            
            if(dst.equals(src.substring(0,src.indexOf('/')+1)) || dst.equals(src))    // ignore back_ward connections to the parent link & self connections
                continue;

            if(!Pages.containsKey(dst))
                Pages.put(dst,new HashSet<String>());

            HashSet<String>temp = Pages.get(dst);
            temp.add(src);
            Pages.put(dst,temp);

            if(outDegree.containsKey(src))
                outDegree.put(src,outDegree.get(src)+1);
            else
                outDegree.put(src,1);
        }

        for(int i = 0 ; i < Iterations ; i++)
        {
            HashMap<String,Double> temp = new HashMap<>();
            for(HashMap.Entry<String,Double> entry : PagesRank.entrySet())
                temp.put(entry.getKey(), entry.getValue());
            
            for(HashMap.Entry<String,Double> entry : PagesRank.entrySet())
            {
                String Page = entry.getKey();
                if(Pages.containsKey(Page))
                {
                    double rank = 0;
                    HashSet<String> in_URL = Pages.get(Page);
                    Iterator<String> it = in_URL.iterator();
                    while(it.hasNext())
                    {
                        String in = it.next();
                        rank += (temp.get(in)/outDegree.get(in));
                    }
                    entry.setValue(rank);
                }
            }
        }

        //scaling PageRank
        Double maxRank = 0.0;
        for(HashMap.Entry<String,Double> entry : PagesRank.entrySet())
            maxRank = Math.max(maxRank,entry.getValue());
        
        if(maxRank > 0)
            for(HashMap.Entry<String,Double> entry : PagesRank.entrySet())
                entry.setValue(entry.getValue()/maxRank);

        return PagesRank;
    }
}