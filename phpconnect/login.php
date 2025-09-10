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
    $email = $_POST['Email'];
    $password = $_POST['Password'];

    $sql = "SELECT * FROM CommutersDB WHERE Email='$email' AND Password='$password'";
    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        echo json_encode(["success" => true, "message" => "Login successful"]);
    } else {
        echo json_encode(["success" => false, "message" => "Invalid credentials"]);
    }
}

$conn->close();
?>
