package javaLearning.Lambdas;

@FunctionalInterface
interface Mathamatics{
    int operate(int a, int b);
    
}
public class Main {



    public static void main(String[] args) {

        Mathamatics add =(int i, int j)-> i+j;
        Mathamatics subtract =(int i , int j)->i-j;
        Mathamatics multiply =(int i , int j)->i*j;
        Mathamatics division =(int i , int j)->i/j;
        

        System.out.println(add.operate(5, 5));
        System.out.println(subtract.operate(10, 5));
        System.out.println(multiply.operate(5, 5));
        System.out.println(division.operate(10, 5));
        
    }
    
}
