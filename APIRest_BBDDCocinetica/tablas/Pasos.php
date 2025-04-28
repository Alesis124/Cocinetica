<?php
class Pasos {
    private $tabla = "Pasos";
    public $id_paso;
    public $id_receta;
    public $texto;
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    function leerTodos() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla);
        $stmt->execute();
        return $stmt->get_result();
    }

    function leerPorReceta() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_receta = ? ORDER BY id_paso ASC");
        $stmt->bind_param("i", $this->id_receta);
        $stmt->execute();
        return $stmt->get_result();
    }

    function insertar() {
        $this->texto = strip_tags($this->texto);
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (id_receta, texto) VALUES (?, ?)");
        $stmt->bind_param("is", $this->id_receta, $this->texto);
        return $stmt->execute();
    }

    function actualizar() {
        $this->texto = strip_tags($this->texto);
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET texto = ? WHERE id_paso = ?");
        $stmt->bind_param("si", $this->texto, $this->id_paso);
        return $stmt->execute();
    }

    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_paso = ?");
        $stmt->bind_param("i", $this->id_paso);
        return $stmt->execute();
    }
}
?>
