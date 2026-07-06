package lcr.util;

public class Log {
    static public void d(String title, String desc){
        if(Constants.SHOW_LOGS)
            System.out.println(title+":"+desc);
    }
    static public void e(String title, String desc){
        if(Constants.SHOW_LOGS)
            System.err.println(title+":"+desc);
    }
}
