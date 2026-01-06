class arr{

    public static void main(String[] args){

        int arr[] [] = new int[2] [3];
        arr[0][0]=1;
        arr[0][1]=2;
        arr[0][2]=3;
        arr[1][0]=4;
        arr[1][1]=5;
        arr[1][2]=6;

       

        for(int i =0;i<arr.length;i++){
            
            for(int j=0;j<arr[i].length;j++){
                System.out.print(arr[i][j]);
            }

            System.out.println("\n-----");
        }


        for(int n[] : arr){
            
            for(int column : n){
                System.out.print(column);
            }
             System.out.println("\n-+-+-+-+");
        }

        /** three dimensin array */

        int nums[][][]  =new int [3][5][6];

{
    

        for(int i=0;i<nums.length;i++){
           for(int j=0;j<arr[i].length;j++){


           }
        }




        
    }
    
    }
}