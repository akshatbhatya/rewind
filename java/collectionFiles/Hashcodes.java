package collectionFiles;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;


class Student{
    int rollNo;
    String name;

    Student(int rollNo, String name){
        this.rollNo=rollNo;
        this.name=name;
    }

    @Override
    public String toString(){
         return "name : "+ this.name + " "+ "Roll No : "+ this.rollNo;
    }

    @Override
    public int hashCode(){
        return Objects.hash(rollNo);
    }

    @Override
    public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Student student = (Student) o;
            return rollNo == student.rollNo;
    }
}
public class Hashcodes {


    public static  void main(String[] args){

        Set <Student> student = new HashSet();

        student.add(new Student(1, "Akshat"));
        student.add(new Student(2, "akshit"));
         student.add(new Student(2, "jessica"));

        Iterator it = student.iterator();

        while(it.hasNext()){
            System.out.println(it.next());
        }

        for(Student s : student){
            
            System.out.println(s.name);
            System.out.println(s.rollNo);

        }
    }
    
}
