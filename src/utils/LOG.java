package utils;

public class LOG {
    public boolean debug;
    public LOG(boolean debug){ this.debug = debug; }
    public static void info(String s) {
        System.out.println("Info: " + s);
    }

    public static void error(String s) {
        System.out.println("!!Error: " + s);
    }

    public static void stdout(String s) { System.out.println(s); }

    public void debug(String s) {
        if (this.debug) { LOG.stdout("|debug| " + s); }
    }
}
