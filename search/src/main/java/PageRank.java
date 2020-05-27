import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

public class PageRank
{
    public class Pair
    {
        public String first,second;
        public Pair()
        {
            first = "";
            second = "";
        }
        public Pair(String a,String b)
        {
            first = a;
            second = b;
        }
    }
    public static final int Iterations = 10;

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
            else
            {
                HashSet<String>temp = Pages.get(connections.get(i).second);
                temp.add(connections.get(i).first);
                Pages.put(connections.get(i).second,temp);
            }
            PagesRank.put(connections.get(i).first,1.0);
            PagesRank.put(connections.get(i).second,1.0);
            if(outDegree.containsKey(connections.get(i).first))
                outDegree.put(connections.get(i).first,outDegree.get(connections.get(i).first)+1);
            else
                outDegree.put(connections.get(i).first,1);
        }

        for(HashMap.Entry entry : PagesRank.entrySet())
            entry.setValue(1.0/PagesRank.size());

        for(int i = 0 ; i < Iterations ; i++)
        {
            for(HashMap.Entry entry : PagesRank.entrySet())
            {
                String Page = (String) entry.getKey();
                double rank = 0;
                HashSet in_URL = Pages.get(Page);
                Iterator<String> it = in_URL.iterator();
                while(it.hasNext())
                {
                    String in = it.next();
                    if(outDegree.containsKey(in) && !in.contentEquals(Page))
                        rank += (PagesRank.get(in)/outDegree.get(in));
                }
                if(rank > 0)
                    entry.setValue(rank);
            }
        }
        return PagesRank;
    }
}