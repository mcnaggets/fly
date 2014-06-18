package by.fly.util;

import java.util.regex.Pattern;

public class Utils {

    private Utils() {
        // utility
    }

    public static Pattern containsIgnoreCasePattern(String filter) {
        return Pattern.compile("(?i)(?=.*" + filter + ")");
    }

}
