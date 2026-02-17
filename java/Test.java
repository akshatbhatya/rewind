
import java.util.Arrays;



public class Test {
    
    public void shortArray(int [] arr){

        for(int i = 0; i < arr.length; i++){

        for (int j = i+1; j < arr.length; j++) {
            if(arr[j] < arr[i]){

                int temp = arr[i];
                arr[i]=arr[j];
                arr[j] = temp;

            }
        }

        }

        System.out.println(Arrays.toString(arr));
    }

    public static void main(String[] args){

        int [] arr ={3,2,4,5,1};
       


        Test test= new Test();

        test.shortArray(arr);


    }
}
