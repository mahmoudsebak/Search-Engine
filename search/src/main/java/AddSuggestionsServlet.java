import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/addSuggestion")
public class AddSuggestionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		Gson gson = new Gson();
		IndexerDbAdapter adapter = new IndexerDbAdapter();
		adapter.open();
		String suggestion = request.getParameter("suggestion");
		
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		boolean result = adapter.addQuery(suggestion);

		HashMap<String, Boolean> res = new HashMap<>();
		res.put("result", result);
		out.print(gson.toJson(res));
		out.flush();
		adapter.close();
	}

}
