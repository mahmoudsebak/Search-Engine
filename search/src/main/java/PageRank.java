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
        ArrayList<Pair> connections = adapter.fetchAllLinks();
        HashMap<String,Double> ret = CalculatePageRank(connections);
        for(HashMap.Entry<String,Double> entry : ret.entrySet())
            adapter.updateURL(entry.getKey(), entry.getValue());
        long endTime = System.nanoTime();
        adapter.close();
        System.out.println("Taken time : "+ ((endTime-startTime)*1.0/1e9));
    }
    /**
     *
     * @param connections: list of pairs <src url,dst url>
     * @return map of each url and its pageRank
     */
    public static HashMap<String,Double> CalculatePageRank(ArrayList<Pair> connections)
    {
        HashMap<String,Double> PagesRank = new HashMap<String, Double>();
        HashMap<String,Integer> outDegree = new HashMap<String, Integer>();
        HashMap<String,HashSet<String>> Pages = new HashMap<String, HashSet<String>>();

        for(int i = 0 ; i < connections.size() ; i++)
        {
            if(!Pages.containsKey(connections.get(i).second))
                Pages.put(connections.get(i).second,new HashSet<String>());
            
            HashSet<String>temp = Pages.get(connections.get(i).second);
            temp.add(connections.get(i).first);
            Pages.put(connections.get(i).second,temp);

            PagesRank.put(connections.get(i).first,1.0);
            PagesRank.put(connections.get(i).second,1.0);

            if(outDegree.containsKey(connections.get(i).first))
                outDegree.put(connections.get(i).first,outDegree.get(connections.get(i).first)+1);
            else
                outDegree.put(connections.get(i).first,1);
        }

        for(HashMap.Entry<String,Double> entry : PagesRank.entrySet())
            entry.setValue(1.0/PagesRank.size());

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
                        if(!in.contentEquals(Page))
                            rank += (temp.get(in)/outDegree.get(in));
                    }
                    entry.setValue(rank);
                }
            }
        }
        return PagesRank;
    }
}