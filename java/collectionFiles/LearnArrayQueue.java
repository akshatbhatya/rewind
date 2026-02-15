package collectionFiles;

import java.util.ArrayDeque;

public class LearnArrayQueue {
   


    public static void main(String[] args) {
         ArrayDeque <Integer> arr = new ArrayDeque<>();
         arr.offer(1);
         arr.offerFirst(0);
         arr.offerLast(10);
         arr.offer(5);
         
         System.out.println(arr);

         System.out.println(arr.getFirst());
         System.out.println(arr.getLast());

         System.out.println(arr.peek());
         System.out.println(arr.peekFirst());
         System.out.println(arr.peekLast());

         System.out.println(arr.poll());
         System.out.println(arr.pollFirst());
         System.out.println(arr.pollLast());
         System.out.println(arr.pop());
         System.out.println(arr.remove(0));
         System.out.println(arr);

    }
  
}
