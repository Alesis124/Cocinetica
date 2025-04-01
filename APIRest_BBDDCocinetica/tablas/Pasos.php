<?php
class Pasos
{
    private $tabla = "Pasos";
    public $id_paso;
    public $id_receta;
    public $texto;
    public $conn;

    public function __construct($db)
    {
        $this->conn = $db;
    }

    function leer()
    {
        if (isset($this->id_paso)) {
            $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla . " WHERE id_paso = ?");
            $stmt->bind_param("i", $this->id_paso);
        } else {
            $stmt = $this->conn->prepare("SELECT * FROM " . $this->tabla);
        }
        $stmt->execute();
        $result = $stmt->get_result();
        return $result;
    }

    function insertar()
    {
        $stmt = $this->conn->prepare("INSERT INTO " . $this->tabla . "(`id_receta`, `texto`) VALUES(?, ?)");
        $this->id_receta = strip_tags($this->id_receta);
        $this->texto = strip_tags($this->texto);
        $stmt->bind_param("is", $this->id_receta, $this->texto);
        return $stmt->execute();
    }

    function actualizar()
    {
        $stmt = $this->conn->prepare("UPDATE " . $this->tabla . " SET texto = ? WHERE id_paso = ?");
        $this->texto = strip_tags($this->texto);
        $this->id_paso = strip_tags($this->id_paso);
        $stmt->bind_param("si", $this->texto, $this->id_paso);
        return $stmt->execute();
    }
}
?>
