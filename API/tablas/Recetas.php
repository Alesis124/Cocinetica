<?php
class Recetas {
    private $tabla = "Recetas";
    public $id_receta;
    public $nombre;
    public $duracion;
    public $valoracion;
    public $imagen;
    public $id_usuario;
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
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_receta = ?");
        $stmt->bind_param("i", $this->id_receta);
        $stmt->execute();
        return $stmt->get_result();
    }

    function insertar() {
        $this->nombre = strip_tags($this->nombre);
        $this->duracion = strip_tags($this->duracion);
        $this->valoracion = strip_tags($this->valoracion);
        $this->imagen = strip_tags($this->imagen);
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (nombre, duracion, valoracion, imagen, id_usuario) VALUES (?, ?, ?, ?, ?)");
        $stmt->bind_param("sidsi", $this->nombre, $this->duracion, $this->valoracion, $this->imagen, $this->id_usuario);
        return $stmt->execute();
    }

    function actualizar() {
        $this->nombre = strip_tags($this->nombre);
        $this->duracion = strip_tags($this->duracion);
        $this->valoracion = strip_tags($this->valoracion);
        $this->imagen = strip_tags($this->imagen);
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET nombre = ?, duracion = ?, valoracion = ?, imagen = ? WHERE id_receta = ?");
        $stmt->bind_param("sidii", $this->nombre, $this->duracion, $this->valoracion, $this->imagen, $this->id_receta);
        return $stmt->execute();
    }

    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_receta = ?");
        $stmt->bind_param("i", $this->id_receta);
        return $stmt->execute();
    }
}
?>
