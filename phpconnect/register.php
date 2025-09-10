<?php
$servername = "localhost";
$username = "u987478351_CommutersDB"; 
$password = "komyu696969"; 
$dbname = "u987478351_Komyu"; 

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user_name = $_POST['User_name'];
    $email = $_POST['Email'];
    $password = $_POST['Password'];
    $fname = $_POST['F_name'];
    $lname = $_POST['L_name'];
    $dob = $_POST['Date_Of_Birth'];
    $address = $_POST['Address'];
    $age = $_POST['Age'];
    $phone = $_POST['Phone_Number'];

    $sql = "INSERT INTO CommutersDB (User_name, Email, Password, F_name, L_name, Date_Of_Birth, Address, Age, Phone_Number) 
            VALUES ('$user_name', '$email', '$password', '$fname', '$lname', '$dob', '$address', '$age', '$phone')";

    if ($conn->query($sql) === TRUE) {
        echo json_encode(["success" => true, "message" => "Registration successful"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }
}

$conn->close();
?>
