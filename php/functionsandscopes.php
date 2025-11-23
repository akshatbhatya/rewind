<?php

// Global variable
$num = 10;

/* 
    Function to print global variable 
*/
function printNumber() {
    // Access global variable inside function
    global $num;
    echo "Global num value: " . $num;
}

printNumber();
echo "<br>";
echo "Outside num is: $num";
echo "<br><br>";

/*
    Static variable example
    - Static variable value function calls के बीच में retain रहती है
*/
function rememberData() {
    static $num = 2;
    $num++; // Increase by 1
    $num++; // Increase by 1 again
    echo "Static num value: " . $num;
}

rememberData();
echo "<br>";
rememberData();
echo "<br><br>";

/*
    Pass by reference example
    - &$x means x की original value modify होगी
*/
function addByRef(&$x, $y) {
    $x = $x + $y;
}

$a = 5;
addByRef($a, 3); // $a becomes 8
echo "Value of a after addByRef(): $a";
echo "<br><br>";

/*
    String length example
*/
$naame = "akshat";
echo "Length of '$naame' is: " . strlen($naame);
echo "<br><br>";

/*
    Recursive factorial function
*/
function factorial($n) {
    if ($n == 0) {
        return 1; // Base case
    } else {
        return $n * factorial($n - 1); // Recursive call
    }
}

echo "Factorial using recursion: " . factorial(5);
echo "<br><br>";

/*
    Factorial using loop (Iterative method)
*/
function factorialWay($n) {
    $num = 1;
    if ($n == 0) return 1;

    for ($n; $n > 0; $n--) {
        $num = $num * $n;
    }
    return $num;
}

echo "Factorial using loop: " . factorialWay(5);


function addSpace(){
    echo "<br><br>";
}

/*

1. String Functions
strlen(): Returns the length of a string.
strtoupper(): Converts a string to uppercase.
strtolower(): Converts a string to lowercase.
substr(): Returns a part of a string.
ucfirst() to make a capital  first leter
ucwords() to  make a first leter capital of all sence 
*/



addSpace();

echo " String Functions";

$str="this is string";

addSpace();
echo "length of string is ". strlen($str);
addSpace();
echo strtoupper($str);
addSpace();
echo strtolower($str);
addSpace();

echo "sub string ". substr($str,1,2);
addSpace();

echo "uc first ". ucfirst($str);

addSpace();

echo "ucword ". ucwords($str);


/*

2. Array Functions
array_push(): Adds elements to the end of an array.
array_pop(): Removes the last element of an array.
array_merge(): Merges two or more arrays.

*/
addSpace();

echo "2. Array Functions";

addSpace();

$arr=[1,2,3,4,5,6,7,8];
$arr2=[55,56];
array_push($arr,11,22,33);
print_r($arr);

array_pop($arr);
echo "<pre>";
print_r($arr);
echo "</pre>";
addSpace();
$newArr=array_merge($arr,$arr2);
print_r($newArr);


/* 

3. Date and Time Functions
date(): Returns the current date or time.
strtotime(): Converts a string into a Unix timestamp.
time(): Returns the current Unix timestamp.


*/

addSpace();

echo "Date Functions";

