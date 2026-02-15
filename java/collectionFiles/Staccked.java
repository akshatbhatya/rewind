package collectionFiles;

import java.util.Stack;

public class Staccked {
    // last in first out 


    public static void main(String[] args) {
        
        Stack<String> elements = new Stack<String>();
        elements.push("lion");
        elements.push("dog");
        elements.push("cat");

        System.out.println(elements);

        System.err.println(elements.peek());

        elements.pop();
        System.err.println(elements.peek());
     }
}
