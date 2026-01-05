public class loops {

    public static void main(String args[]){

        for(int i=0; i<200;i++){
            System.out.println(i);
        }

        int i=0;

        while(true){
            if(i==20)break;

            System.out.println(i);
            i++;
        }
        System.out.println("bye"+ i);
    }
    
}
