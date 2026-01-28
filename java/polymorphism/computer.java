package polymorphism;

class Laptop{
    public void turnOn(){
        System.out.println("laptop is on");
    }
    public final void turnOff(){
        System.err.println("turn off");

    }
}

class Features extends Laptop{

    @Override
    public void turnOn() {
        System.out.println("Laptop with UI layers is on");
    }


    public void ui(){
        System.out.print("Ui Layers");
    }
}

class Charger extends Features{
    public void charger(){
        System.out.println("charging start");
    }


    // @Override
    // public void turnOff(){
        
    // }
}

public class computer {


    public static void main(String[] args) {

    Charger ch = new Charger();
    ch.charger();

    Laptop cha =new Charger();
    cha.turnOn();
    cha = new Features();
    cha.turnOn();
    
        
    }
    
}
