package abstraction;

abstract class Car{

    public abstract void startCar();
    public abstract void engine();

    public void exhaustSound(){
        System.out.println("brom brom");
    }
}


abstract class Suzuki extends Car{

    public void startCar(){
        System.out.println("car suzuki started");
    }
}

class UpdatedSuzuki extends Suzuki{

@Override 
public void engine(){
    System.out.println("1200cc");
}

}

public class Main {

   public static void main(String[] args) {
        Car suzuki =new UpdatedSuzuki();
        suzuki.exhaustSound();
        suzuki.startCar();
        suzuki.engine();
   }


    
}
