package javaLearning.CollectionApis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Student{
    int rollNo;
    String name;

    public Student(int rollNo , String name){
        this.name=name;
        this.rollNo=rollNo;
    }
}

public class Main {
    

    public static void main(String[] args) {

        // Collection<Integer> nums = new ArrayList <Integer> ();
        // nums.add(5);
        // nums.add(12);
        // nums.add(30);
        // System.out.println(nums);

        // List<Integer> nums = new ArrayList <Integer> ();
        // nums.add(5);
        // nums.add(12);
        // nums.add(30);
        // System.out.println(nums.get(2));
        // System.out.println(nums);

        // Set <Integer> nums = new HashSet <Integer> ();
        // nums.add(5);
        // nums.add(12);
        // nums.add(30);
        // System.out.println(nums);

        // Iterator <Integer> values = nums.iterator();

        // while (values.hasNext()) {
        //     System.out.println(values.next());
        // }

        Map <String, Integer> obj = new HashMap<>();
        obj.put("akshat", 1);
        obj.put("simran",2);
        obj.put("akshit", 3);
        obj.put("akku", 4);
        
        for(String key : obj.keySet()){
            System.out.println(key + "=>"+ obj.get(key));
        }

        List<Student> obj1 = new ArrayList<>();

        obj1.add(new Student(1,"akshat"));
        obj1.add(new Student(2,"akshit"));



       

        // for(int i : nums){
        //     System.out.println(i);
        // }
      
        
    }
    

}
