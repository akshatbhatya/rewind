class Demo{

    public Demo(){
        super();
        System.out.println(" class demo clalled");
    }

    public Demo(int num) {
       System.out.println(num);
    }
}


class B extends Demo{
    public B(){
        super();
        System.out.println("b is called");
    }

    public B(int num){
        super(num);
        System.out.println("num is : "+num);
    }
}


public class A {
    public static void main(String[] args){
        B b1=new B();
        B b2 = new B(5);
        // System.out.print(b1);
    }
}

        