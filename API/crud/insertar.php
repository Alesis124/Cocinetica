<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type");

include_once '../basededatos/cocinetica.php';   
include_once '../tablas/Comentarios.php';
include_once '../tablas/Contiene.php';
include_once '../tablas/Guarda.php';
include_once '../tablas/Ingredientes.php';
include_once '../tablas/Pasos.php';
include_once '../tablas/Recetas.php';
include_once '../tablas/UM.php';
include_once '../tablas/Usuarios.php';
include_once '../tablas/Valoraciones.php';

$database = new Cocinetica();
$conex = $database->dameConexion();

// Leer JSON crudo
$datos = json_decode(file_get_contents("php://input"));

if (!isset($datos->tabla)) {
    http_response_code(400);
    echo json_encode(["error" => "Tabla no especificada"]);
    exit();
}

switch ($datos->tabla) {
    case 'Comentarios':
        $obj = new Comentarios($conex);
        $obj->texto = $datos->texto;
        $obj->id_usuario = $datos->id_usuario;
        $obj->id_receta = $datos->id_receta;
        break;
    case 'Contiene':
        $obj = new Contiene($conex);
        $obj->id_receta = $datos->id_receta;
        $obj->id_ingrediente = $datos->id_ingrediente;
        $obj->id_um = $datos->id_um;
        $obj->cantidad = $datos->cantidad;
        break;
    case 'Guarda':
        $obj = new Guarda($conex);
        $obj->id_usuario = $datos->id_usuario;
        $obj->id_receta = $datos->id_receta;
        break;
    case 'Ingredientes':
        $obj = new Ingredientes($conex);
        $obj->nombre = $datos->nombre;
        break;
    case 'Pasos':
        $obj = new Pasos($conex);
        $obj->id_receta = $datos->id_receta;
        $obj->texto = $datos->texto;
        break;
    case 'Recetas':
        $obj = new Recetas($conex);
        $obj->nombre = $datos->nombre;
        $obj->duracion = $datos->duracion;
        $obj->valoracion = $datos->valoracion;
        $obj->imagen = $datos->imagen;
        $obj->id_usuario = $datos->id_usuario;
        break;
    case 'Usuarios':
        $obj = new Usuarios($conex);
        $obj->correo = $datos->correo;
        $obj->usuario = $datos->usuario;
        $obj->descripcion = $datos->descripcion;
        $obj->imagen = $datos->imagen;
        break;
    case 'Valoraciones':
        $obj = new Valoraciones($conex);
        $obj->id_usuario = $datos->id_usuario;
        $obj->id_receta = $datos->id_receta;
        $obj->valoracion = $datos->valoracion;  // Cambiar de valor a valoracion
        // Para manejar comentario relacionado si quieres incluirlo
        if (isset($datos->id_comentario)) {
            $obj->id_comentario = $datos->id_comentario;
        }
        break;

    default:
        http_response_code(400);
        echo json_encode(["error" => "Tabla no válida"]);
        exit();
}

if ($obj->insertar()) {
    http_response_code(201);

    $respuesta = ["mensaje" => "Insertado correctamente"];

    switch ($datos->tabla) {
        case 'Comentarios':
            $respuesta["id_comentario"] = $obj->id_comentario;
            break;
        case 'Recetas':
            $respuesta["id_receta"] = $obj->id_receta;
            break;
        // Puedes añadir más casos si otras tablas devuelven un ID
    }

    echo json_encode($respuesta);

} else {
    http_response_code(503);
    echo json_encode(["error" => "Error al insertar"]);
}


?>
