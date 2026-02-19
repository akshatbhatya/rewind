package javaLearning.Dsa.sortingTechniques;

import java.util.Arrays;

public class BubbleSort {


    public void sortUsingBubble(int [] arr , int size){
        for(int i=0; i<size; i++){

            for(int j=0; j<size -i-1 ;j++){
                if(arr[j]> arr[j+1]){
                 int temp = arr[j];
                 arr[j] = arr[j+1];
                 arr[j+1]=temp;
                
                }
            }
        }

        System.out.println(Arrays.toString(arr));
    }

    public static void main(String[] args) {

        int[] arr ={4,5,2,9,3,6,1};
        /*
         
        {4,5,2,3,9,6,1}; 0
        {4,5,2,3,6,9,1}; 1
        {4,5,2,3,6,1,9}; 2
        {4,5,2,3,1,6,9}; 3
        {4,5,2,1,3,6,9}; 4
        {4,5,1,2,3,6,9}; 5
        {4,5,1,2,3,6,9}; 6


        loop 2 0 t0 5
        {1,5,2,3,4,6,9}; 0
        {1,2,3,4,5,6,9}; 1
        

        */
       
        BubbleSort sorting =new BubbleSort();
        sorting.sortUsingBubble(arr,arr.length);
    }

    
}
