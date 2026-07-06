package lcr.util;

public class Log {
    static public void d(String title, String desc){
        System.out.println(title+":"+desc);
    }
    static public void e(String title, String desc){
        System.err.println(title+":"+desc);
    }
}
