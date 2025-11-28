<?php
/** associative arrays */

$array= [
    "name"=>"akshat",
    "class"=>"cse"
];


$keys=array_keys($array);



for($i=0;$i<count($keys);$i++){
    echo "keys $keys[$i] => value is : ".$array[$keys[$i]]."\n";

    $array[$keys[$i]."test"]= "test value";
}

print_r($array);

foreach ($array as $key => $value) {
   echo "key is :".$key."value is :".$value."\n";
}