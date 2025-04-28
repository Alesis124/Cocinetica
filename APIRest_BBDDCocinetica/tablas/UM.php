<?php
class UM {
    private $tabla = "UM";
    public $id_um;
    public $medida;
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
        $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_um = ?");
        $stmt->bind_param("i", $this->id_um);
        $stmt->execute();
        return $stmt->get_result();
    }
}
?>
