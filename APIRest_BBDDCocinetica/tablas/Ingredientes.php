<?php
class Ingredientes
{
    private $tabla = "Ingredientes";
    public $id_ingrediente;
    public $nombre;
    public $conn;
    public function __construct($db)
    {
        $this->conn = $db;
    }
    function leer()
    {
        if($this->id_ingrediente >= 0){
            $stmt = $this->conn->prepare("SELECT * FROM ". $this->tabla. " WHERE id_ingrediente = ?");
            $stmt->bind_param("i", $this->id_ingrediente);
        }else{
            $stmt = $this->conn->prepare("SELECT * FROM ". $this->tabla);
        }
        $stmt->execute();
        $result = $stmt->get_result();
        return $result;
    }

    function insertar()
    {
        $stmt = $this->conn->prepare("INSERT INTO ". $this->tabla. " (nombre) VALUES (?)");
        $this->nombre = strip_tags($this->nombre);
        $stmt->bind_param("s", $this->nombre);
        $stmt->bind_param("s", $this->nombre);

        if($stmt->execute()){
            
            return true;
        }else{
            return false;
        }
    }



}
?>