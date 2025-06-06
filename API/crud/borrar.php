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


$datos = json_decode(file_get_contents("php://input"));

if (!isset($datos->tabla)) {
    http_response_code(400);
    echo json_encode(["error" => "Tabla no especificada"]);
    exit();
}

switch ($datos->tabla) {
    case 'Comentarios':
        $obj = new Comentarios($conex);
        $obj->id_comentario = $datos->id_comentario;
        break;
    case 'Contiene':
        $obj = new Contiene($conex);
        $obj->id_receta = $datos->id_receta;
        $obj->id_ingrediente = $datos->id_ingrediente;
        break;
    case 'Guarda':
        $obj = new Guarda($conex);
        $obj->id_usuario = $datos->id_usuario;
        $obj->id_receta = $datos->id_receta;
        break;
    case 'Ingredientes':
        $obj = new Ingredientes($conex);
        $obj->id_ingrediente = $datos->id_ingrediente;
        break;
    case 'Pasos':
        $obj = new Pasos($conex);
        $obj->id_paso = $datos->id_paso;
        break;
    case 'Recetas':
        $obj = new Recetas($conex);
        $obj->id_receta = $datos->id_receta;
        break;
    case 'Usuarios':
        $obj = new Usuarios($conex);
        $obj->id_usuario = $datos->id_usuario;
        break;
    case 'Valoraciones':
        $obj = new Valoraciones($conex);
        $obj->id_valoracion = $datos->id_valoracion;
        break;

    default:
        http_response_code(400);
        echo json_encode(["error" => "Tabla no válida"]);
        exit();
}

if ($obj->borrar()) {
    http_response_code(200);
    echo json_encode(["mensaje" => "Borrado correctamente"]);
} else {
    http_response_code(503);
    echo json_encode(["error" => "Error al borrar"]);
}
?>
