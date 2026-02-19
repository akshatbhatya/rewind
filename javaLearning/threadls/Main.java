package javaLearning.threadls;


class Task extends Thread{

    @Override
    public void run(){

        for(int i=0;i<100;i++){
        System.out.println("hi  running");

        try {
            Thread.sleep(1);
        }
        catch (InterruptedException err) {
           System.out.println(err);
        }
        
        }
    }

}

class TaskTwo extends Thread{
    public void run(){
         for(int i=0;i<100;i++){
        System.out.println("thread 2 called");

        try {
            Thread.sleep(1);
        }
        catch (Exception err) {
           System.out.println(err);
        }

         }
    }
}

class TaskThree implements Runnable{
    public void run(){
        for(int i=0;i<20;i++){
            System.out.println("runnable called");
        }

        try {
            Thread.sleep(20);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class Counter implements Runnable {
    int count = 0;

    public synchronized void increment() {
        count++;
    }

    // @Override
    public  void run() {
        for (int i = 0; i < 10000; i++) {
            this.increment();
        }
    }
}




public class Main {

    public static void main(String[] args) {
        Task      task =  new Task();
        TaskTwo   task2 = new TaskTwo();
        TaskThree task3 = new TaskThree();
        Counter counter =new Counter();
        


        Thread t1 = new Thread(task3);
        Thread t2 = new Thread(counter);
        Thread t3 = new Thread(counter);
        // t1.setPriority(3);
        // task.setPriority(9);
        // task2.setPriority(9);

        // task.start();
        // task2.start();
        
        // t1.start();
        t2.start();
        t3.start();
        try {
            t2.join();
            t3.join();
        }
        catch (Exception err) {
            System.out.println(err.getMessage());
        }

        System.out.println(counter.count);
    }
    
}
