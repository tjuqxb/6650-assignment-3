import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "StatisticsServlet")
public class StatisticsServlet extends HttpServlet {
    private Gson gson = new Gson();

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        if (urlPath != null && !urlPath.isEmpty()) {
            String[] urlParts = urlPath.split("/");
            if (!isUrlValid(urlParts, "GET")) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write(Utils.getReturnMessage(gson, "invalid url"));
                return;
            }
        }
        res.setStatus(HttpServletResponse.SC_OK);
        // do any sophisticated processing with urlParts which contains all the url params
        res.getWriter().write("It works for GET!");
    }

    /**
     * Validate if the url is valid based on categories.
     *
     * @param urlPath the segmented url path to be validated
     * @param source indicating the category of request, e.g., POST or GET
     * @return a boolean value indicating the url is valid or not
     */
    private boolean isUrlValid(String[] urlPath, String source) {
        //GET/statistics
        if (source.equals("GET")) {
            return urlPath.length == 0 || (urlPath.length == 1 && urlPath[0].length() == 0);
        }
        return false;
    }
}
