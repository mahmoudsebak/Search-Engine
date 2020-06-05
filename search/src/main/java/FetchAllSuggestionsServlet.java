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

@WebServlet("/getSuggestions")
public class FetchAllSuggestionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
		Gson gson = new Gson();
		IndexerDbAdapter adapter = new IndexerDbAdapter();
		adapter.open();
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		ArrayList<String> result = adapter.fetchAllQueries();

		HashMap<String, ArrayList<String>>res = new HashMap<>();
		res.put("result", result);
		out.print(gson.toJson(res));
		out.flush();
		adapter.close();
	}

}
