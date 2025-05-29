<?php
class Contiene {
    private $tabla = "Contiene";
    public $id_receta;
    public $id_ingrediente;
    public $id_um;
    public $cantidad;
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
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_receta = ?");
        $stmt->bind_param("i", $this->id_receta);
        $stmt->execute();
        return $stmt->get_result();
    }

    function insertar() {
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . " (id_receta, id_ingrediente, id_um, cantidad) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("iiid", $this->id_receta, $this->id_ingrediente, $this->id_um, $this->cantidad);
        return $stmt->execute();
    }

    function actualizar() {
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET cantidad = ?, id_um = ? WHERE id_receta = ? AND id_ingrediente = ?");
        $stmt->bind_param("diii", $this->cantidad, $this->id_um, $this->id_receta, $this->id_ingrediente);
        return $stmt->execute();
    }

    public function leerPorIdReceta($id_receta) {
        $sql = "SELECT * FROM Contiene WHERE id_receta = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_receta);
        $stmt->execute();
        return $stmt->get_result();
    }


    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_receta = ? AND id_ingrediente = ?");
        $stmt->bind_param("ii", $this->id_receta, $this->id_ingrediente);
        return $stmt->execute();
    }
}
?>
