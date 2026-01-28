package ObjectOveriding;

class Data{

    String name;
    int rollNo;

    public void layer(){
        System.out.println("No layer is present");
    }

    public void setInformation(String name , int rollNo){
        this.name = name;
        this. rollNo = rollNo;
    }

    public String toString(){
         return name + " >> " + rollNo;
    }
}

public class objectovr {
    


    public static void main(String[] args) {
        Data data =new Data();
        data.setInformation("akshat", 1);
        System.out.println(data.toString());
    }
}

