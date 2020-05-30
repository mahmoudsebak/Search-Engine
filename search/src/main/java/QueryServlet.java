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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@WebServlet("/query")
public class QueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String query = request.getParameter("query");
		int page = Integer.parseInt(request.getParameter("page"));
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Gson gson = new Gson();

		ArrayList<HashMap<String, String>> result = QueryProcessor.ProcessQuery(query,page);
		
		// add title of the pages in response
		for (HashMap<String,String> pageEntry : result) {
			Document doc;

			try {
				doc = Jsoup.connect(pageEntry.get("url")).get();
				String title = doc.title();
				pageEntry.put("title", title);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}

		HashMap<String, Object> res = new HashMap<>();
		res.put("result", result);
		out.print(gson.toJson(res));
		out.flush();
	}

}
