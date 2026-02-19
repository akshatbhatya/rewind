package javaLearning.collectionFiles;

import java.util.LinkedList;
import java.util.Queue;

public class LearnQueue {

    public static void main(String[] args) {
        
        Queue<String> str = new LinkedList<String>();

        str.offer("akshat");
        str.offer("akshit");
        str.offer("malik");


        System.out.println(str);
        System.out.println("Element at Peek "+ str.peek());
        System.out.println("Remove the element "+ str.poll());
        System.err.println(str);
    }
    
}
