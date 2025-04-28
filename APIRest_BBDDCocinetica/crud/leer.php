<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=UTF-8");

include_once '../basededatos/cocinetica.php';

if (!isset($_GET['tabla'])) {
    http_response_code(400);
    echo json_encode(["info" => "No se especificó tabla"]);
    exit;
}

$tabla = strtolower($_GET['tabla']);
$database = new Cocinetica();
$conex = $database->dameConexion();

// Cargar la tabla
switch ($tabla) {
    case "recetas":
        include_once '../tablas/Recetas.php';
        $objeto = new Recetas($conex);
        $idCampo = "id_receta";
        break;
    case "usuarios":
        include_once '../tablas/Usuarios.php';
        $objeto = new Usuarios($conex);
        $idCampo = "id_usuario";
        break;
    case "comentarios":
        include_once '../tablas/Comentarios.php';
        $objeto = new Comentarios($conex);
        $idCampo = "id_comentario";
        break;
    case "ingredientes":
        include_once '../tablas/Ingredientes.php';
        $objeto = new Ingredientes($conex);
        $idCampo = "id_ingrediente";
        break;
    case "pasos":
        include_once '../tablas/Pasos.php';
        $objeto = new Pasos($conex);
        $idCampo = "id_paso";
        break;
    case "guarda":
        include_once '../tablas/Guarda.php';
        $objeto = new Guarda($conex);
        $idCampo = ""; // No hay ID único simple
        break;
    case "contiene":
        include_once '../tablas/Contiene.php';
        $objeto = new Contiene($conex);
        $idCampo = ""; // No hay ID único simple
        break;
    case "um":
        include_once '../tablas/UM.php';
        $objeto = new UM($conex);
        $idCampo = "id_um";
        break;
    default:
        http_response_code(400);
        echo json_encode(["info" => "Tabla inválida"]);
        exit;
}

// Ahora hacemos la lectura
if (!empty($idCampo) && isset($_GET[$idCampo])) {
    $objeto->$idCampo = intval($_GET[$idCampo]);
    $result = $objeto->leerUno();
} else {
    $result = $objeto->leerTodos();
}

// Procesamos el resultado
if ($result->num_rows > 0) {
    if ($result->num_rows == 1) {
        $registro = $result->fetch_assoc();
        http_response_code(200);
        echo json_encode($registro);
    } else {
        $datos = [];
        while ($fila = $result->fetch_assoc()) {
            array_push($datos, $fila);
        }
        http_response_code(200);
        echo json_encode($datos);
    }
} else {
    http_response_code(404);
    echo json_encode(["info" => "No se encontraron datos"]);
}
?>
