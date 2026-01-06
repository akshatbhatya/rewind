class Data{
    public Data(String name){
        System.out.println("custrutor is called"+ " "+ name);
    }

    public void showMessage() {
        System.out.println("Method is called!");
    }
}



public class Pen {
    public static void main(String[] args){

        Data data =new Data("akshat");
        data.showMessage();

    }
}
