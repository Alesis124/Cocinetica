<?php
class Guarda {
    private $tabla = "Guarda";
    public $id_usuario;
    public $id_receta;
    public $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    function leer() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla);
        $stmt->execute();
        return $stmt->get_result();
    }

    function insertar() {
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (id_usuario, id_receta) VALUES (?, ?)");
        $stmt->bind_param("ii", $this->id_usuario, $this->id_receta);
        return $stmt->execute();
    }

    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_usuario = ? AND id_receta = ?");
        $stmt->bind_param("ii", $this->id_usuario, $this->id_receta);
        return $stmt->execute();
    }
}
?>
