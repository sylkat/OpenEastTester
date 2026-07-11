package lcr.util;

/**
 * Utility logging wrapper providing centralized console output redirection
 * gated by global system tracking visibility flags.
 * * @author sylkat
 */
public class Log {

    /**
     * Prints a standardized debug message stream trace to standard output (stdout)
     * if the global runtime logging visibility toggle is enabled.
     * * @param title the prefix subsystem or identifier key string
     * @param desc  the detailed descriptive message body context payload
     */
    public static void d(String title, String desc) {
        if (Constants.SHOW_LOGS) {
            System.out.println(title + ":" + desc);
        }
    }

    /**
     * Prints a standardized error tracking alert message to standard error (stderr)
     * if the global runtime logging visibility toggle is enabled.
     * * @param title the prefix subsystem or failure identifier key string
     * @param desc  the detailed error or exception context narrative payload
     */
    public static void e(String title, String desc) {
        if (Constants.SHOW_LOGS) {
            System.err.println(title + ":" + desc);
        }
    }
}