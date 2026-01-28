package UpCasting;


class A{

    public void show(){
        System.out.println("show");

    }

}

class B extends A{

    public void show2(){
        System.out.println("show 2");
    }

}

public class upcasting {
    
    public static void main(String[] args){

        A a =(A) new B();
        a.show();

        B b = new B();
        

    }
}


