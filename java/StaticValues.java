import java.util.jar.Attributes.Name;

class Phone{
    String Name;
    String Model;
    static int Price;

    public Phone(){
        System.out.println("constructor called");
    }

    static{
        Price =30000;
    }

     public static void printNew(Phone phone)  {
        System.out.println("static method is called"+ Price + " " + phone.Name + " " + phone.Model);
    }
}


public class StaticValues {

    public static void main(String[] args){
        Phone n1 = new Phone();
        n1.Name="Apple";
        n1.Model="4s";
        n1.Price=25000;

        Phone n2= new Phone();
        n2.Name="samsung";
        n2.Model="M35";
        n2.Price=18000;

        Phone[] data =new Phone[2];
        data[0]= n1;
        data[1]=n2;

        for(Phone values : data){
            System.out.println(values.Name);
            System.out.println(values.Model);
            System.out.println(values.Price);
            System.out.println("\n");
        }

        Phone.printNew(n2);
    }
    
}
