package utils;

public class Logger {
    public Logger(){ }
    public static void info(String s) {
        System.out.println("Info: " + s);
    }

    public static void error(String s) {
        System.out.println("!!Error: " + s);
    }

    public static void stdout(String s) { System.out.println(s); }
}
