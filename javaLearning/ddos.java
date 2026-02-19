package javaLearning;
import java.net.HttpURLConnection;
import java.net.URL;

// 1. Helper Class: Jo website hit karegi
// (Isko public class ke bahar rakhein)
class WebsiteHitter implements Runnable {
    String threadName;
    String targetUrl = "https://www.w3schools.com/";

    public WebsiteHitter(String name) {
        this.threadName = name;
    }

    @Override
    public void run() {
        while (true) { 
            try {
                URL url = new URL(targetUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); 
                
                int responseCode = connection.getResponseCode();
                System.out.println(threadName + " ne hit kiya. Status: " + responseCode);

                // Safety Delay (2 Seconds)
                Thread.sleep(2000); 

            } catch (Exception e) {
                System.out.println(threadName + " Error: " + e.getMessage());
            }
        }
    }
}

// 2. Main Class: Jiska naam file ke naam (ddos.java) jaisa hoga
public class ddos {
    
    public static void main(String[] args) {
        System.out.println("Starting 4 Threads...");

        Thread t1 = new Thread(new WebsiteHitter("Thread 1"));
        Thread t2 = new Thread(new WebsiteHitter("Thread 2"));
        Thread t3 = new Thread(new WebsiteHitter("Thread 3"));
        Thread t4 = new Thread(new WebsiteHitter("Thread 4"));

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}