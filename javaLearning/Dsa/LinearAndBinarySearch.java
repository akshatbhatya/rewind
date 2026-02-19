package javaLearning.Dsa;

public class LinearAndBinarySearch {
    

    public int linearSearch(int[] arr, int num){

        for(int i=0; i<arr.length;i++){

            if (arr[i]==num) {
                return i;
            }
        }
        return -1;

    }

    public int binarySearch(int [] arr, int num){
        int start= 0;
        int end= arr.length-1;
       int midElement = (start+end)/2;  // strt index zero  last element index get the length


       while (start<= end) { 

        if (arr[midElement]==num) {
            return midElement;
        }   

        if(num> arr[midElement]){
            start = midElement + 1;
        }

        if(num< arr[midElement]){
            end = midElement-1; 
        }

        midElement = (start+end)/2;

       }

       return  -1;
       

    }
   


    public static void main(String[] args) {
        int arr []={1,2,3,4,5,6,7,8};
        int num=5;

        LinearAndBinarySearch find = new LinearAndBinarySearch();

       int res= find.linearSearch(arr,num);
       int binaryRes =find.binarySearch(arr, num);

       System.out.println(res);
       System.out.println(binaryRes);

        
    }

 
}
