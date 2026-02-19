package javaLearning.collectionFiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {




    public static void main(String[] args){

        ArrayList<String> newArr = new ArrayList<String>();

        newArr.add("akshat");
        newArr.add("akshit");

        List <Integer> arr2 = new ArrayList<Integer>();

        arr2.add(2);
        arr2.add(3);
        arr2.add(4);
        arr2.add(1,23);  // add values using a index 
        arr2.remove(2);

        arr2.set(0,100); // update the value 

        System.out.println("index of 0 is : "+arr2.get(0));
        System.out.println(newArr);
        System.err.println(arr2);

        System.out.println(arr2.contains(100));

       // arr2.clear();  // to  remove all elements 
        System.out.println(arr2);

        for(int i=0;i<arr2.size();i++){
            System.out.println("element is : "+ arr2.get(i));
        }

        //  foreach loop 

        for(Integer element : arr2){
            System.out.println(element);

        }

        //  without itrator using only a while loop bro 

        Iterator it = arr2.iterator();

        while(it.hasNext()){
            System.out.println("elemnt num is "+ it.next());
        }
    }
    
}
