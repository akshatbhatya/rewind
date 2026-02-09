package threadls;


class Task extends Thread{

    @Override
    public void run(){

        for(int i=0;i<200;i++){
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
         for(int i=0;i<200;i++){
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

public class Main {

    public static void main(String[] args) {
        Task task = new Task();
        TaskTwo task2 =new TaskTwo();

        task.start();
        task2.start();
    }
    
}
