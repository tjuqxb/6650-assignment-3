import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import domain.LiftRide;
import domain.ResortPost;
import domain.SkierPost;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@WebServlet(name = "SkierServlet0",
        value = {"/SkierServlet"})
public class SkierServlet extends HttpServlet {
    private Gson gson = new Gson();
    private final static String QUEUE_NAME_1 = "skier_service_queue";
    private final static String QUEUE_NAME_2 = "resort_service_queue";
    private BlockingQueue<Channel> channelPool = new ArrayBlockingQueue<Channel>(200);
    private EventCountCircuitBreaker breaker = new EventCountCircuitBreaker(2000, 2, TimeUnit.SECONDS, 1600);
    private AtomicInteger exp = new AtomicInteger(1);


    public SkierServlet() {
        super();
    }

    public void init(ServletConfig config) {
        // use hard coded private IP address here for the RabbitMQ address
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.31.27.239");
        try {
            Connection conn = factory.newConnection();
            for (int i = 0; i < 200; i++) {
                Channel ch = conn.createChannel();
                channelPool.offer(ch);
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
            return;
        }
        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts, "POST")) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if (!validatePostJson(sb.toString())) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write(Utils.getReturnMessage(gson,"invalid json POST"));
                return;
            }
            res.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            res.getWriter().write("It works for POST!");
            Integer resortID = Utils.parseNum(urlParts[1]);
            Integer seasonID = Utils.parseNum(urlParts[3]);
            Integer daysID = Utils.parseNum(urlParts[5]);
            Integer skierID = Utils.parseNum(urlParts[7]);
            SkierPost post = gson.fromJson(sb.toString(),SkierPost.class);
            LiftRide ride = new LiftRide(resortID, seasonID, daysID, skierID, post.getLiftID(), post.getTime(), post.getWaitTime());
            String message = gson.toJson(ride);
            Boolean retry = true;
            while (retry) {
                if (breaker.incrementAndCheckState()) {
                    Channel channel = null;
                    try {
                        channel = channelPool.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    channel.queueDeclare(QUEUE_NAME_1, true, false, false, null);
                    channel.basicPublish("", QUEUE_NAME_1, null, message.getBytes(StandardCharsets.UTF_8));
                    channel.queueDeclare(QUEUE_NAME_2, true, false, false, null);
                    channel.basicPublish("", QUEUE_NAME_2, null, message.getBytes(StandardCharsets.UTF_8));
                    channelPool.offer(channel);
                    retry = false;
                } else {
                    int uExp = Math.min(14, exp.getAndIncrement());
                    int maxSleepTime = (int)Math.pow(2.0, (double)uExp);
                    int sleepTime = (int)(Math.random() * maxSleepTime);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts, "GET")) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(Utils.getReturnMessage(gson,"invalid url"));
        } else {
            if (urlParts.length == 3) {
                String resortStr = req.getParameter("resort");
                String skierIDStr = req.getParameter("skierID");
                String seasonIDStr = req.getParameter("season");
                Integer resortID = Utils.parseNum(resortStr);
                Integer skierID = Utils.parseNum(skierIDStr);
                Integer seasonID = Utils.parseNum(skierIDStr);
                if (resortID == null || skierID == null || (seasonIDStr != null && seasonID == null)) {
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.getWriter().write(Utils.getReturnMessage(gson,"invalid parameters"));
                    return;
                }
            }
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            res.getWriter().write("It works for GET!");
            res.getWriter().write(urlPath);
        }
    }

    /**
     * Validate the url based on categories.
     *
     * @param urlPath the segmented url path to be validated
     * @param source indicating the category of request, e.g., POST or GET
     * @return a boolean value indicating the url is valid or not
     */
    private boolean isUrlValid(String[] urlPath, String source) {
        // urlPath  = "/LAB2_2_war_exploded/skiers/1/seasons/2019/days/1/skiers/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]

        //  /skiers   /{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID} (GET POST)
        //  /skiers    /{skierID}/vertical (GET)

        if (urlPath.length == 8){
            if (urlPath[0].length() != 0) return false;
            Integer resortID = Utils.parseNum(urlPath[1]);
            if (resortID == null) return false;
            if (!urlPath[2].equals("seasons")) return false;
            Integer seasonID = Utils.parseNum(urlPath[3]);
            if (seasonID == null) return false;
            if (!urlPath[4].equals("days")) return false;
            Integer daysID = Utils.parseNum(urlPath[5]);
            if (daysID == null) return false;
            if (daysID < 1 || daysID > 366) return false;
            if (!urlPath[6].equals("skiers")) return false;
            Integer skierID = Utils.parseNum(urlPath[7]);
            return skierID != null;
        } else if (source.equals("GET") && urlPath.length == 3) {
            if (urlPath[0].length() != 0) return false;
            Integer skierID = Utils.parseNum(urlPath[1]);
            if (skierID == null) return false;
            return urlPath[2].equals("vertical");
        }
        return false;
    }

    /**
     * Validate the JSON string.
     *
     * @param str the JSON string to be validated
     * @return a boolean value indicating whether the JSON string is valid or not
     */
    private boolean validatePostJson(String str) {
        SkierPost post = gson.fromJson(str, SkierPost.class);
        Map map = gson.fromJson(str, Map.class);
        if (map.size() != 3) return false;
        return post.getLiftID() != null && post.getTime() != null && post.getWaitTime() != null;
    }



}
