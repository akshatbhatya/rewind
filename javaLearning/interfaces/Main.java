package javaLearning.interfaces;

interface A{

   void show();
   void configs();
}

interface B extends A{
}

class C implements A{
   
   @Override
   public  void show() {
       System.out.println("show");
   }

 
   
   @Override
   public void configs(){
      System.out.println("configs");

   }
}
public class Main {

   public static void main(String[] args){
      C c= new C();
      c.configs();
   }
    
}
