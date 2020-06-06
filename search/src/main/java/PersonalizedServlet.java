import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/userAction")
public class PersonalizedServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        URL url = new URL(request.getParameter("url"));
        String BaseLink = url.getProtocol() + "://" + url.getHost();
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();
        adapter.addUserURL(BaseLink);
        adapter.close();
	}

}