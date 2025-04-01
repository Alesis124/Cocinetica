<?php
class Comentarios {
    private $tabla = "Comentarios";
    public $id_comentario;
    public $texto;
    public $fecha;
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
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (texto, id_usuario, id_receta) VALUES (?, ?, ?)");
        $stmt->bind_param("sii", $this->texto, $this->id_usuario, $this->id_receta);
        return $stmt->execute();
    }

    function actualizar() {
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET texto = ? WHERE id_comentario = ?");
        $stmt->bind_param("si", $this->texto, $this->id_comentario);
        return $stmt->execute();
    }

    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_comentario = ?");
        $stmt->bind_param("i", $this->id_comentario);
        return $stmt->execute();
    }
}
?>