package inheritanceLearn;

class CaclulatorCls extends advanceClass{
    public int addition(int a,int b){
        return a+b;
    }


    public int subtract(int a,int b){
        return a-b;
    }
}

public class calc{


    public static void main(String[] args){

    CaclulatorCls a = new CaclulatorCls();
    System.out.println(a.addition(4 ,5));
    System.out.println(a.divide(5, 2));
    System.out.println(a.subtract(4, 4));
    System.out.println(a.multi(4, 4));

}

}


