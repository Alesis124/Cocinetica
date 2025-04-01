<?php
class Usuarios {
    private $tabla = "Usuarios";
    public $id_usuario;
    public $correo;
    public $contraseña;
    public $nombre;
    public $descripcion;
    public $imagen;
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
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (correo, contraseña, nombre, descripcion, imagen) VALUES (?, ?, ?, ?, ?)");
        $stmt->bind_param("sssss", $this->correo, $this->contraseña, $this->nombre, $this->descripcion, $this->imagen);
        return $stmt->execute();
    }

    function actualizar() {
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET correo = ?, contraseña = ?, nombre = ?, descripcion = ?, imagen = ? WHERE id_usuario = ?");
        $stmt->bind_param("sssssi", $this->correo, $this->contraseña, $this->nombre, $this->descripcion, $this->imagen, $this->id_usuario);
        return $stmt->execute();
    }

    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_usuario = ?");
        $stmt->bind_param("i", $this->id_usuario);
        return $stmt->execute();
    }
}
?>