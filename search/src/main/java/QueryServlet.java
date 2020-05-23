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

@WebServlet("/query")
public class QueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String query = request.getParameter("query");
		int page = Integer.parseInt(request.getParameter("page"));
		ArrayList<HashMap<String, String>> result = null;
		IndexerDbAdapter dbAdapter = new IndexerDbAdapter();
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();
		
		dbAdapter.open();

		// phrase matching
		if (query.startsWith("\"") && query.endsWith("\"")) {
			result = dbAdapter.queryPhrase(query.substring(1, query.length()-1), 10, page);
		}
		else {
			String[] words = query.split(" ");
			result = dbAdapter.queryWords(words, 10, page);

		}

		dbAdapter.close();
		
		out.print(gson.toJson(result));
		out.flush();
	}

}
