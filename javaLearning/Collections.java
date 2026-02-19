package javaLearning;
class Student {
    String name;
    int rollNo;
    int marks;

}

public class Collections {



    public static void main(String[] args){
        /* collections */

        Student student1 = new Student();
        Student student2 = new Student();
        Student student3 = new Student();

        student1.name="akshat";
        student1.rollNo=123;
        student1.marks=78;

        student2.name="akshit";
        student2.rollNo=123;
        student2.marks=78;

        student3.name="akshita";
        student3.rollNo=123;
        student3.marks=78;

        Student students[] =new Student[3];
        students[0]=student1;
        students[1]=student2;
        students[2]=student3;

        for(Student i : students){
            System.err.println(i.name);
            System.err.println(i.marks);
            System.err.println(i.rollNo);

        }






    }
    
}
