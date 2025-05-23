<?php
class Valoraciones {
    private $tabla = "Valoraciones";
    public $id_valoracion;  // clave primaria para borrar, actualizar, leer
    public $id_usuario;
    public $id_receta;
    public $valoracion;
    public $id_comentario; // puede ser NULL
    private $conn;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Leer todos los registros
    function leerTodos() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla);
        $stmt->execute();
        return $stmt->get_result();
    }

    // Insertar nuevo registro
    function insertar() {
        if (isset($this->id_comentario)) {
            $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (id_usuario, id_receta, valoracion, id_comentario) VALUES (?, ?, ?, ?)");
            $stmt->bind_param("iiii", $this->id_usuario, $this->id_receta, $this->valoracion, $this->id_comentario);
        } else {
            $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (id_usuario, id_receta, valoracion) VALUES (?, ?, ?)");
            $stmt->bind_param("iii", $this->id_usuario, $this->id_receta, $this->valoracion);
        }
        return $stmt->execute();
    }

    // Borrar por id_valoracion
    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_valoracion = ?");
        $stmt->bind_param("i", $this->id_valoracion);
        return $stmt->execute();
    }

    // Actualizar registro completo
    function actualizar() {
        if (isset($this->id_comentario)) {
            $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET id_usuario = ?, id_receta = ?, valoracion = ?, id_comentario = ? WHERE id_valoracion = ?");
            $stmt->bind_param("iiiii", $this->id_usuario, $this->id_receta, $this->valoracion, $this->id_comentario, $this->id_valoracion);
        } else {
            $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET id_usuario = ?, id_receta = ?, valoracion = ?, id_comentario = NULL WHERE id_valoracion = ?");
            $stmt->bind_param("iiii", $this->id_usuario, $this->id_receta, $this->valoracion, $this->id_valoracion);
        }
        return $stmt->execute();
    }

    // Leer una valoración por id_valoracion
    function leerUno() {
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_valoracion = ?");
        $stmt->bind_param("i", $this->id_valoracion);
        $stmt->execute();
        return $stmt->get_result();
    }

    // Obtener promedio de valoracion por receta
    function obtenerPromedioPorReceta($id_receta) {
        $stmt = $this->conn->prepare("SELECT AVG(valoracion) as promedio FROM " . $this->tabla . " WHERE id_receta = ?");
        $stmt->bind_param("i", $id_receta);
        $stmt->execute();
        return $stmt->get_result()->fetch_assoc()['promedio'];
    }
}
?>
