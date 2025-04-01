<?php
class UM
{
    private $tabla = "UM";
    public $id_um;
    public $medida;
    public $connection;

    public function __construct($db){

        $this->connection = $db;
    }
    function leer(){
        if($this->id_um >= 0){
            $stmt = $this->connection->prepare("SELECT * FROM $this->tabla WHERE id_um = ?");
            $stmt->bind_param("i", $this->id_um);
        }else{
            $stmt = $this->connection->prepare("SELECT * FROM $this->tabla");
        }
        $stmt->execute();
        $result = $stmt->fetch();
        return $result;
    }
}
?>