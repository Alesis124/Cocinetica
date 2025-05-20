    <?php
class Cocinetica
{
    private $host = 'mariadb'; // IP de la Raspberry Pi
    private $port = 3306;            // puerto MariaDB
    private $user = 'root';
    private $password = "D9Qj6yGMdzEREcz";
    private $database = "Cocinetica";

    public function dameConexion()
    {
        $conn = new mysqli($this->host, $this->user, $this->password, $this->database, $this->port);
        $conn->set_charset('utf8');
        if ($conn->connect_error) {
            die("Error al conectar con MYSQL: ". $conn->connect_error);
        } else {
            return $conn;
        }
    }
}

?>
