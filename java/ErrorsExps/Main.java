package ErrorsExps;

public class Main {


    public static void main(String[] args) {
        int i=0;
        int j=12;
        try{

            int ans = j/i;
            System.out.println(ans);

        }catch(Exception e){
            System.out.println(e);
        }
    }
    
}
