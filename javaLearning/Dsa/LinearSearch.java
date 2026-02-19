package javaLearning.Dsa;

public class LinearSearch {
    

    public int linearSearch(int[] arr, int num){

        for(int i=0; i<arr.length;i++){

            if (arr[i]==num) {
                return i;
            }
        }
        return -1;

    }
   


    public static void main(String[] args) {
        int arr []={1,4,8,8,5,11};
        int num=5;

        LinearSearch find = new LinearSearch();

       int res= find.linearSearch(arr,num);

       System.out.println(res);

        
    }

 
}
