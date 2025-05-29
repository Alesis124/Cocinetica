<?php
class Ingredientes {
    private $tabla = "Ingredientes";
    public $id_ingrediente;
    public $nombre;
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    function leerTodos() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla);
        $stmt->execute();
        return $stmt->get_result();
    }

    function leerUno() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_ingrediente = ?");
        $stmt->bind_param("i", $this->id_ingrediente);
        $stmt->execute();
        return $stmt->get_result();
    }

    function insertar() {
        $this->nombre = strip_tags($this->nombre);
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (nombre) VALUES (?)");
        $stmt->bind_param("s", $this->nombre);
        return $stmt->execute();
    }
}
?>
