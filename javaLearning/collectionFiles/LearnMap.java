package javaLearning.collectionFiles;

import java.util.HashMap;
import java.util.Map;

public class LearnMap {
    



    public static void main(String[] args){


        Map<String,Integer> data = new HashMap<>();
        data.put("akshat", 1);
        data.put("akshit",2);
        data.put("human", 3);

       for(String key : data.keySet()){
          System.out.println("key is "+key +" and vlaue is "+ data.get(key));
       }
    }
}
