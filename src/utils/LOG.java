package utils;

public class LOG {
    public LOG(){ }
    public static void info(String s) {
        System.out.println("Info: " + s);
    }

    public static void error(String s) {
        System.out.println("!!Error: " + s);
    }

    public static void stdout(String s) { System.out.println(s); }
}
