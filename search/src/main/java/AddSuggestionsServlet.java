import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

@WebServlet("/addSuggestion")
public class AddSuggestionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		Gson gson = new Gson();
		IndexerDbAdapter adapter = new IndexerDbAdapter();
		adapter.open();
		
		Properties props = new Properties();
    	props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		String query = request.getParameter("suggestion");
		String region = request.getParameter("region");

		CoreDocument doc = new CoreDocument(query);
		
		pipeline.annotate(doc);
		
		int detectedPersons = 0;
		for (CoreEntityMention em : doc.entityMentions()) {
			if (em.entityType().equals("PERSON")) {
				adapter.addTrend(em.text(), region);
				detectedPersons++;
			}
		}

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		boolean result = adapter.addQuery(query);

		HashMap<String, Object> res = new HashMap<>();
		res.put("result", result);
		res.put("detected persons", detectedPersons);
		out.print(gson.toJson(res));
		out.flush();
		adapter.close();
	}

}
