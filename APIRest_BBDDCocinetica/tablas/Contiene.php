<?php
class Contiene
{
    private $tabla = "Contiene";
    public $id_receta;
    public $id_ingrediente;
    public $id_um;
    public $cantidad;
    public $conn;

    public function __construct($db)
    {
        $this->conn = $db;
    }

    function leer()
    {
        if (isset($this->id_receta)) {
            $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_receta = ?");
            $stmt->bind_param("i", $this->id_receta);
        } else {
            $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla);
        }
        $stmt->execute();
        $result = $stmt->get_result();
        return $result;
    }

    function insertar()
    {
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . "(`id_receta`, `id_ingrediente`, `id_um`, `cantidad`) VALUES(?, ?, ?, ?)");
        $this->id_receta = strip_tags($this->id_receta);
        $this->id_ingrediente = strip_tags($this->id_ingrediente);
        $this->id_um = strip_tags($this->id_um);
        $this->cantidad = strip_tags($this->cantidad);
        $stmt->bind_param("iiid", $this->id_receta, $this->id_ingrediente, $this->id_um, $this->cantidad);
        return $stmt->execute();
    }

    function actualizar()
    {
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET cantidad = ?, id_um = ? WHERE id_receta = ? AND id_ingrediente = ?");
        $this->cantidad = strip_tags($this->cantidad);
        $this->id_um = strip_tags($this->id_um);
        $this->id_receta = strip_tags($this->id_receta);
        $this->id_ingrediente = strip_tags($this->id_ingrediente);
        $stmt->bind_param("diii", $this->cantidad, $this->id_um, $this->id_receta, $this->id_ingrediente);
        return $stmt->execute();
    }
    function borrar()
    {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_receta = ? AND id_ingrediente = ?");
        $this->id_receta = strip_tags($this->id_receta);
        $this->id_ingrediente = strip_tags($this->id_ingrediente);
        $stmt->bind_param("ii", $this->id_receta, $this->id_ingrediente);
        return $stmt->execute();
    }
}
?>
