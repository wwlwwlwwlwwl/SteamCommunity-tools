package cn.wwl;

public class Main {

    public static void main(String[] args) {
        /*
        try {
        ConsoleManager consoleManager = ConsoleManager.getInstance();
        consoleManager.test();
        Thread.sleep(3000);
        consoleManager.flushConsole();
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        MarketTools.getInstance().startApplication();
    }
}
