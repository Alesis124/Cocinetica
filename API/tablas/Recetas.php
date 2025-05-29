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

    // Leer todas las recetas
    function leerTodos() {
        $stmt = $this->conn->prepare(
            "SELECT r.*, u.usuario 
             FROM " . $this->tabla . " r
             JOIN Usuarios u ON r.id_usuario = u.id_usuario"
        );
        $stmt->execute();
        return $stmt->get_result();
    }

    // Leer una receta específica
    function leerUno() {
        $stmt = $this->conn->prepare(
            "SELECT r.*, u.usuario 
             FROM " . $this->tabla . " r
             JOIN Usuarios u ON r.id_usuario = u.id_usuario
             WHERE r.id_receta = ?"
        );
        $stmt->bind_param("i", $this->id_receta);
        $stmt->execute();
        return $stmt->get_result();
    }

    // Leer recetas guardadas por un usuario
    function leerGuardadasPorUsuario($id_usuario) {
        $stmt = $this->conn->prepare("
            SELECT r.*, u.usuario 
            FROM Guarda g
            JOIN Recetas r ON g.id_receta = r.id_receta
            JOIN Usuarios u ON r.id_usuario = u.id_usuario
            WHERE g.id_usuario = ?
        ");
        $stmt->bind_param("i", $id_usuario);
        $stmt->execute();
        return $stmt->get_result();
    }


    // Insertar nueva receta
    function insertar() {
        $this->nombre = strip_tags($this->nombre);
        $this->duracion = strip_tags($this->duracion);
        $this->valoracion = strip_tags($this->valoracion);
        $this->imagen = strip_tags($this->imagen);

        $stmt = $this->conn->prepare(
            "INSERT INTO " . $this->tabla . " (nombre, duracion, valoracion, imagen, id_usuario) 
            VALUES (?, ?, ?, ?, ?)"
        );
        $stmt->bind_param("sidsi", $this->nombre, $this->duracion, $this->valoracion, $this->imagen, $this->id_usuario);
        
        if ($stmt->execute()) {
            $this->id_receta = $this->conn->insert_id;
            return true;
        }

        return false;
    }


    function buscar($texto) {
        $like = '%' . $texto . '%';
        $stmt = $this->conn->prepare("
            SELECT DISTINCT r.*, u.usuario
            FROM Recetas r
            JOIN Usuarios u ON r.id_usuario = u.id_usuario
            LEFT JOIN Contiene c ON r.id_receta = c.id_receta
            LEFT JOIN Ingredientes i ON c.id_ingrediente = i.id_ingrediente
            WHERE r.nombre LIKE ? OR i.nombre LIKE ?
        ");
        $stmt->bind_param("ss", $like, $like);
        $stmt->execute();
        return $stmt->get_result();
    }


    // Actualizar receta existente
    function actualizar() {
        // Solo sanitizar campos de texto
        if (isset($this->nombre)) {
            $this->nombre = strip_tags($this->nombre);
        }
        if (isset($this->duracion)) {
            $this->duracion = strip_tags($this->duracion);
        }
        if (isset($this->valoracion)) {
            $this->valoracion = strip_tags($this->valoracion);
        }
        
        // NO sanitizar la imagen - mantenerla exactamente como viene
        // $this->imagen = strip_tags($this->imagen); // <- ELIMINAR ESTA LÍNEA

        // Construir consulta dinámica
        $updates = [];
        $params = [];
        $types = '';

        if (isset($this->nombre)) {
            $updates[] = "nombre = ?";
            $params[] = $this->nombre;
            $types .= 's';
        }
        if (isset($this->duracion)) {
            $updates[] = "duracion = ?";
            $params[] = $this->duracion;
            $types .= 'i';
        }
        if (isset($this->valoracion)) {
            $updates[] = "valoracion = ?";
            $params[] = $this->valoracion;
            $types .= 'd';
        }
        if (isset($this->imagen)) {
            $updates[] = "imagen = ?";
            $params[] = $this->imagen;
            $types .= 's';
        }

        if (empty($updates)) {
            return false;
        }

        $params[] = $this->id_receta;
        $types .= 'i';

        $sql = "UPDATE " . $this->tabla . " SET " . implode(', ', $updates) . " WHERE id_receta = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param($types, ...$params);
        
        return $stmt->execute();
    }

    public function leerPorUsuario($id_usuario) {
        $query = "SELECT * FROM Recetas WHERE id_usuario = ?";
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("i", $id_usuario);
        $stmt->execute();
        return $stmt->get_result();
    }


    // Borrar una receta
    function borrar() {
        $stmt = $this->conn->prepare("DELETE FROM " . $this->tabla . " WHERE id_receta = ?");
        $stmt->bind_param("i", $this->id_receta);
        return $stmt->execute();
    }
}
?>
