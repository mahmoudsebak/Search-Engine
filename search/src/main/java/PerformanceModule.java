import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class PerformanceModule {
    public static boolean isDone = false;
    public static void main(String[] args) throws InterruptedException
    {
        String req = "http://localhost:8080/search/query?query=movie&page=1";
        PerformanceModule x = new PerformanceModule();
        for(int threads_num=1;threads_num<10000;threads_num+=10)
        {
            ArrayList<Thread> threads = new ArrayList<>();
            for(int i = 1;i<=threads_num;i++)
                threads.add(new Thread(x.new user(req, threads_num)));

            long startTime = System.currentTimeMillis();
            for(int i=0;i<threads.size();i++)
                threads.get(i).start();
            for(int i=0;i<threads.size();i++)
                threads.get(i).join();
            long endTime = System.currentTimeMillis();
            System.out.println("Processing " + threads_num + " requests took " + (endTime-startTime) + " ms.");
            if(isDone)
            {
                System.out.println("Timed out when users num = " + threads_num);
                break;
            }
        }
    }

    class user implements Runnable
    {
        String req = null;
        int user_num = 0;
        public user(String req,int n)
        {
            this.req = req;
            user_num = n;
        }
        public void run()
        {
            long startTime = System.currentTimeMillis();
            try
            {
                PerformanceModule.testRequest(req);
            }catch(Exception e)
            {
                System.out.println("Exception occured with number of users:" + user_num);
                isDone = true;
                return;
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Request took "+ (endTime-startTime) + " ms");
        }
    }
    public static void testRequest(String urlString) throws IOException {
        HttpURLConnection urlConnection = null;
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
    }
}