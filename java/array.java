public class array {
    
    public static void main(String[] args){

        int[] arr={1,2,3,4,5,6};

        int sum[]={};
        int index= 0;
        for (int i : arr) {
            System.out.println(i);
            sum[index]=i;
            i++;
        }

        System.out.println(sum);
    }
}
