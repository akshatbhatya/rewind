package javaLearning.collectionFiles;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class LearnPriorityQueue {
    

    public static void main(String[] args) {
        Queue <Integer> arr = new PriorityQueue<Integer>(Comparator.reverseOrder());
        arr.offer(1);
        arr.offer(2);
        arr.offer(3);
        arr.offer(4);
        arr.offer(5);

        System.out.println(arr);
        System.err.println("Peak Value in  the Priority Queue : "+ arr.peek());

    }
}
