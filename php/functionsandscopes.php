<?php

$num=10;


function printNumber(){
    /** access global  variable */
    global $num;
    echo $num;
}

printNumber();

echo "<br>";
echo "num is : $num";


function rememberData(){
    static $num= 2;
    $num++;
    $num++;
    echo $num;
}

echo "<br>";
rememberData();
echo "<br>";

rememberData();


function addByRef(&$x, $y) {
    $x = $x + $y;
}
$a = 5;
addByRef($a, 3);
echo $a;  