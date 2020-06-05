import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet("/userAction")
public class PersonalizedServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request)
			throws ServletException, IOException {
		String url = request.getParameter("url");
        String BaseLink = url.substring(0,url.indexOf('/')+1);
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();
        adapter.addUserURL(BaseLink);
        adapter.close();
	}

}