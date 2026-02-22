package javaLearning.Dsa.sortingTechniques;

import java.util.Arrays;

public class SelectionSort {
    
    /* selction  sort  */


    public void slectionSort(int [] arr, int size){

        for(int i=0;i<size;i++){
            int minIndex=i;
            boolean isTrue =false;

            for(int j= i+1 ;j<size;j++){

                if(arr[minIndex] > arr[j]){
                    minIndex=j;
                    isTrue =true;
                }

            }

            if(isTrue){
                 int temp = arr[minIndex];
                arr[minIndex] = arr[i];
                arr[i] = temp;
            }
            
        }
        System.out.println(Arrays.toString(arr));

    }


    public static void main(String[] args) {
        
        int [] arr={4,6,3,9,5};
        int size = arr.length;

        SelectionSort slection = new SelectionSort();
        slection.slectionSort(arr, size);

    }
}
