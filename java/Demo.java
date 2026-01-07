
class Human{

    private String name;
    private int age;

    public void setName(String name){
        this.name =name;
    }
    public void setAge(int age){
        this.age=age;
    }

     public String getName(){
        return name;
    }
    public int getAge(){
       return age;
    }

}




public class Demo {
    


    /* class opps encasulatons */

    public static void main(String[] args){

        Human h1= new Human();

        h1.setAge(20);
        h1.setName("akshat");

        System.out.println(h1.getName());
        System.out.println(h1.getAge());
    }
}
