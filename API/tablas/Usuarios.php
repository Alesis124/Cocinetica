<?php
class Usuarios {
    private $tabla = "Usuarios";
    public $id_usuario;
    public $correo;
    public $usuario;
    public $descripcion;
    public $imagen;
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    function leerTodos() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla);
        $stmt->execute();
        return $stmt->get_result();
    }

    function insertar() {
        $this->correo = trim(strip_tags($this->correo));
        $this->usuario = trim(strip_tags($this->usuario));
        $this->descripcion = trim(strip_tags($this->descripcion));
        $this->imagen = trim(strip_tags($this->imagen));
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (correo, usuario, descripcion, imagen) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("ssss", $this->correo, $this->usuario, $this->descripcion, $this->imagen);
        return $stmt->execute();
    }

    function actualizar() {
        $this->correo = trim(strip_tags($this->correo));
        $this->usuario = trim(strip_tags($this->usuario));
        $this->descripcion = trim(strip_tags($this->descripcion));
        $this->imagen = trim(strip_tags($this->imagen));
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET correo = ?, usuario = ?, descripcion = ?, imagen = ? WHERE id_usuario = ?");
        $stmt->bind_param("ssssi", $this->correo, $this->usuario, $this->descripcion, $this->imagen, $this->id_usuario);
        return $stmt->execute();
    }

    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_usuario = ?");
        $stmt->bind_param("i", $this->id_usuario);
        return $stmt->execute();
    }
}
?>
