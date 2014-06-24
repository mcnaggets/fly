package by.fly.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {
        // utility
    }

    public static Pattern containsIgnoreCasePattern(String filter) {
        return Pattern.compile("(?i)(?=.*" + filter + ")");
    }

    public static String getCurrentMachineHardwareAddress() {
        try {
            return Arrays.toString(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress());
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }
    }

}
