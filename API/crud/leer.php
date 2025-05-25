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

$result = null;  // Inicializamos $result en null

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

        if (isset($_GET['id_receta'])) {
            $objeto->id_receta = intval($_GET['id_receta']);
            $result = $objeto->leerPorReceta();
        }
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

        if (isset($_GET['id_receta'])) {
            $objeto->id_receta = intval($_GET['id_receta']);
            $result = $objeto->leerPorReceta();
        }
        break;
    case "guarda":
        if (isset($_GET['id_usuario'])) {
            include_once '../tablas/Recetas.php';
            $objeto = new Recetas($conex);
            $result = $objeto->leerGuardadasPorUsuario(intval($_GET['id_usuario']));
        } else {
            include_once '../tablas/Guarda.php';
            $objeto = new Guarda($conex);
            $result = $objeto->leerTodos();
        }

        if ($result->num_rows > 0) {
            $datos = [];
            while ($fila = $result->fetch_assoc()) {
                $datos[] = $fila;
            }
            http_response_code(200);
            echo json_encode($datos);
        } else {
            http_response_code(404);
            echo json_encode([]);
        }

        exit;
    case "contiene":
        include_once '../tablas/Contiene.php';
        $objeto = new Contiene($conex);
        $idCampo = ""; // No hay ID único simple

        if (isset($_GET['id_receta'])) {
            $id_receta = intval($_GET['id_receta']);
            $result = $objeto->leerPorIdReceta($id_receta);
        }
        break;
    case "um":
        include_once '../tablas/UM.php';
        $objeto = new UM($conex);
        $idCampo = "id_um";
        break;
    case "valoraciones":
        include_once '../tablas/Valoraciones.php';
        $objeto = new Valoraciones($conex);
        $idCampo = "id_valoracion";
        break;

    default:
        http_response_code(400);
        echo json_encode(["info" => "Tabla inválida"]);
        exit;
}

// Si estamos en la tabla de usuarios y se pasa un correo, buscar por correo
if ($tabla == "usuarios" && isset($_GET['correo'])) {
    $correo = $_GET['correo'];
    $stmt = $conex->prepare("SELECT * FROM Usuarios WHERE correo = ?");
    $stmt->bind_param("s", $correo);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $registro = $result->fetch_assoc();
        http_response_code(200);
        echo json_encode($registro);
    } else {
        http_response_code(404);
        echo json_encode(["info" => "Usuario no encontrado"]);
    }
    exit;
}

// Solo hacer lectura general si $result aún no se ha asignado
if ($result === null) {
    if (!empty($idCampo) && isset($_GET[$idCampo])) {
        $objeto->$idCampo = intval($_GET[$idCampo]);
        $result = $objeto->leerUno();
    } else {
        $result = $objeto->leerTodos();
    }
}

// Procesamos el resultado
if ($result->num_rows > 0) {
    // Si estamos en la tabla comentarios, siempre devolvemos un array, aunque haya sólo uno
    if ($tabla === "comentarios") {
        $datos = [];
        while ($fila = $result->fetch_assoc()) {
            $datos[] = $fila;
        }
        http_response_code(200);
        echo json_encode($datos);
    } else {
        // Para otras tablas, dejamos la lógica original
        if ($result->num_rows == 1) {
            $registro = $result->fetch_assoc();
            http_response_code(200);
            echo json_encode($registro);
        } else {
            $datos = [];
            while ($fila = $result->fetch_assoc()) {
                $datos[] = $fila;
            }
            http_response_code(200);
            echo json_encode($datos);
        }
    }
} else {
    http_response_code(404);
    echo json_encode(["info" => "No se encontraron datos"]);
}

?>
