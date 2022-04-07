public class Utils {

    /**
     * parse the string to an integer
     *
     * @param str the string to be parsed to an integer
     * @return the parsed integer or null if parse failed
     */
    public static Integer parseSingleNumber(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}