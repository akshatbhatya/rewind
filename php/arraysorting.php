<?php 

$arr=[7,4,5,6,10,4,3,2];


function findMaxNumber($arr){
    $max=$arr[0];
    for($i=0;$i<count($arr);$i++){

        if($arr[$i]>$max){
            $max=$arr[$i];
        }

    }
    return $max;
}

// echo findMaxNumber($arr);
echo "\n";

function findMin($arr){
    $min=$arr[0];

    for($i=0;$i<count($arr);$i++){

        if($arr[$i]<$min){
            $min=$arr[$i];
        }

    }
    return $min;
}

// echo findMin($arr);

function sorting(&$arr){
    $arr2=[];

    $flag=false;
    for($i=0;$i<count($arr);$i++){
        $flag=false;

        for($j=$i;$j<count($arr2);$j++){
            if($arr[$i]<$arr[$j]){
                $temp = $arr[$i];
                $a=$arr[$i];
                $b=$arr[$j];
                $arr[$j]=$b;
               
            }
        }

        
    }

}
sorting($arr);

print_r($arr);