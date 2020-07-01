import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/trends")
public class TrendsServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();
        
        String region = request.getParameter("region");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		Gson gson = new Gson();
		

		ArrayList<HashMap<String, Object>> result = adapter.fetchTrends(region);
		HashMap<String, Object> res = new HashMap<>();
		res.put("result", result);
		out.print(gson.toJson(res));
        out.flush();
        adapter.close();
	}
}