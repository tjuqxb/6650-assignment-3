import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
     * parse the string to an integer
     *
     * @param str the string to be parsed to an integer
     * @return the parsed integer or null if parse failed
     */
    public static Integer parseNum(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * create a standard JSON message string
     *
     * @param gson the Gson instance
     * @param message the message content
     * @return a standard JSON message string
     */
    public static String getReturnMessage(Gson gson, String message) {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        return gson.toJson(map);
    }


}
