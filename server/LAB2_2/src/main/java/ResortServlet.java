import com.google.gson.Gson;
import domain.ResortPost;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@WebServlet(name = "ResortServlet")
public class ResortServlet extends HttpServlet {
    private Gson gson = new Gson();

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        //res.getWriter().write(urlPath);
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson, "invalid url"));
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (isUrlValid(urlParts, "POST") == -1) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson, "invalid url"));
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if (!validatePostJson(sb.toString())) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write(Utils.getReturnMessage(gson, "invalid json POST"));
                return;
            }
            res.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            res.getWriter().write("It works for POST!");
            res.getWriter().write(sb.toString());
        }
    }



    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        if (urlPath != null && !urlPath.isEmpty()) {
            String[] urlParts = urlPath.split("/");
            int code = isUrlValid(urlParts, "GET");
            if (code == -1) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write(Utils.getReturnMessage(gson, "invalid url"));
                return;
            } else if (code == -2) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write(Utils.getReturnMessage(gson, "invalid resort id"));
                return;
            }
        }
        res.setStatus(HttpServletResponse.SC_OK);
        // do any sophisticated processing with urlParts which contains all the url params
        res.getWriter().write("It works for GET!");
        if (urlPath != null) {
            res.getWriter().write(urlPath);
        }
    }

    /**
     * Validate the JSON string.
     *
     * @param str the JSON string to be validated
     * @return a boolean value indicating whether the JSON string is valid or not
     */
    private boolean validatePostJson(String str) {
        ResortPost post = gson.fromJson(str, ResortPost.class);
        Map map = gson.fromJson(str, Map.class);
        if (map.size() != 1) return false;
        if (post.getYear() == null) return false;
        try {
            Integer year = Integer.parseInt(post.getYear());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Validate if the url is valid based on categories.
     *
     * @param urlPath the segmented url path to be validated
     * @param source indicating the category of request, e.g., POST or GET
     * @return an integer indicating if the url is valid.
     *          0: valid
     *         -1: invalid (except for invalid resort ID)
     *         -2: invalid resort ID
     */
    private int isUrlValid(String[] urlPath, String source) {
        // urlPath  = "/1/seasons/2019/day/1/skiers/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skiers, 123]
        //GET/resorts
        //GET/resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
        //GET/resorts/{resortID}/seasons
        //POST/resorts/{resortID}/seasons
        if (source.equals("GET")) {
            if (urlPath.length == 0 || (urlPath.length == 1 && urlPath[0].length() == 0)) {
                return 0;
            }
            if (urlPath.length == 3) {
                if (urlPath[0].length() != 0) return -1;
                Integer resortID = Utils.parseNum(urlPath[1]);
                if (resortID == null) return -1;
                if (!urlPath[2].equals("seasons")) return -1;
                return 0;
            }
            if (urlPath.length == 7) {
                if (urlPath[0].length() != 0) return -1;
                Integer resortID = Utils.parseNum(urlPath[1]);
                if (resortID == null) return -2;
                if (!urlPath[2].equals("seasons")) return -1;
                Integer seasonID = Utils.parseNum(urlPath[3]);
                if (seasonID == null) return -1;
                if (!urlPath[4].equals("days")) return -1;
                Integer daysID = Utils.parseNum(urlPath[5]);
                if (daysID == null) return -1;
                if (daysID < 1 || daysID > 366) return -1;
                if(!urlPath[6].equals("skiers")) return -1;
                return 0;
            }
        } else if (source.equals("POST")) {
            if (urlPath.length == 3) {
                if (urlPath[0].length() != 0) return -1;
                Integer resortID = Utils.parseNum(urlPath[1]);
                if (resortID == null) return -2;
                if (!urlPath[2].equals("seasons")) return -1;
                return 0;
            }
        }
        return -1;
    }


}
