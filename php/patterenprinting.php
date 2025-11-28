<?php

/** patteren printing */



function printStar($n){

    for ($i=0; $i < $n; $i++) { 
      
        for($j=0;$j<=$i;$j++){
            echo "* ";
        }
        echo "\n";
    }
}

// printStar(5);


function printTrianglleStar($n){
    for($i=0;$i<$n;$i++){

     for($j=$n-$i;$j>=1;$j--){
        echo " ";
     }

     for($k=0;$k<=$i;$k++){
        echo "* ";
     }
     echo "\n";

    }
}

// printTrianglleStar(5);

function diamondPattern($n){
    for ($i=0; $i < $n; $i++) { 

        for($j=$n-$i;$j>=1;$j--){
            echo " ";
        }

        for($k=0;$k<$i;$k++){
            echo "* ";
        }

        echo "\n";
    }

    for($o=0;$o<$n;$o++){

        for($l=0;$l<$o;$l++){
            echo " ";
        }

        for($m=$n-$o;$m>=1;$m--){
            echo "* ";
        }
        echo "\n";
    }

}

diamondPattern(5);