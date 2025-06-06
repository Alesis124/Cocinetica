-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Servidor: mariadb
-- Tiempo de generación: 27-05-2025 a las 09:39:39
-- Versión del servidor: 11.7.2-MariaDB-ubu2404
-- Versión de PHP: 8.2.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `Cocinetica`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Comentarios`
--

CREATE TABLE `Comentarios` (
  `id_comentario` int(11) NOT NULL,
  `texto` text NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `id_usuario` int(11) NOT NULL,
  `id_receta` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Comentarios`
--

INSERT INTO `Comentarios` (`id_comentario`, `texto`, `fecha`, `id_usuario`, `id_receta`) VALUES
(1, '¡Esta tarta quedó increíble!', '2025-04-28 08:56:51', 1, 1),
(2, 'Receta rápida y muy rica.', '2025-04-28 08:56:51', 2, 2),
(28, 'no sé si me arrepiento', '2025-05-25 13:33:12', 13, 1),
(30, 'pero si solo tiene dos pasos esta receta', '2025-05-25 15:11:06', 16, 1),
(31, 'pues que solo me dice que eche aceite, ahora tengo aceite caliente', '2025-05-25 15:12:06', 16, 2),
(32, 'se me ha quemado el horno', '2025-05-25 15:12:33', 16, 3),
(34, 'me huelen los pies', '2025-05-26 14:42:21', 14, 3),
(35, 'viva España coñooo', '2025-05-26 14:42:41', 14, 2),
(36, 'tiene que llevar manzana si o si?', '2025-05-26 14:43:14', 14, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Contiene`
--

CREATE TABLE `Contiene` (
  `id_receta` int(11) NOT NULL,
  `id_ingrediente` int(11) NOT NULL,
  `id_um` int(11) NOT NULL,
  `cantidad` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Contiene`
--

INSERT INTO `Contiene` (`id_receta`, `id_ingrediente`, `id_um`, `cantidad`) VALUES
(1, 1, 1, 200.00),
(1, 4, 1, 150.00),
(2, 3, 3, 3.00),
(2, 5, 1, 50.00),
(3, 1, 1, 300.00),
(3, 2, 2, 200.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Guarda`
--

CREATE TABLE `Guarda` (
  `id_usuario` int(11) NOT NULL,
  `id_receta` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Guarda`
--

INSERT INTO `Guarda` (`id_usuario`, `id_receta`) VALUES
(1, 1),
(16, 1),
(1, 2),
(13, 2),
(1, 3),
(3, 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Ingredientes`
--

CREATE TABLE `Ingredientes` (
  `id_ingrediente` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Ingredientes`
--

INSERT INTO `Ingredientes` (`id_ingrediente`, `nombre`) VALUES
(5, 'Aceite de oliva'),
(4, 'Azúcar'),
(6, 'Cb'),
(1, 'Harina'),
(3, 'Huevos'),
(2, 'Leche');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Pasos`
--

CREATE TABLE `Pasos` (
  `id_paso` int(11) NOT NULL,
  `id_receta` int(11) NOT NULL,
  `texto` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Pasos`
--

INSERT INTO `Pasos` (`id_paso`, `id_receta`, `texto`) VALUES
(1, 1, 'Precalentar el horno a 180 grados.'),
(2, 1, 'Mezclar la harina con el azúcar.'),
(3, 2, 'Batir los huevos.'),
(4, 2, 'Freír en una sartén con aceite.'),
(5, 3, 'Mezclar harina y leche.'),
(6, 3, 'Hornear durante 45 minutos.');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Recetas`
--

CREATE TABLE `Recetas` (
  `id_receta` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `duracion` int(11) NOT NULL,
  `valoracion` decimal(3,2) DEFAULT NULL,
  `imagen` longtext DEFAULT NULL,
  `id_usuario` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Recetas`
--

INSERT INTO `Recetas` (`id_receta`, `nombre`, `duracion`, `valoracion`, `imagen`, `id_usuario`) VALUES
(1, 'Tarta de manzana', 90, 2.67, '0', 1),
(2, 'Tortilla Española', 30, 3.50, '0', 2),
(3, 'Bizcocho de Yogur', 45, 3.00, '0', 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `UM`
--

CREATE TABLE `UM` (
  `id_um` int(11) NOT NULL,
  `medida` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `UM`
--

INSERT INTO `UM` (`id_um`, `medida`) VALUES
(5, 'cucharada'),
(1, 'g'),
(4, 'kg'),
(3, 'l'),
(2, 'ml'),
(7, 'pieza'),
(6, 'taza');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Usuarios`
--

CREATE TABLE `Usuarios` (
  `id_usuario` int(11) NOT NULL,
  `correo` varchar(255) NOT NULL,
  `usuario` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `imagen` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Usuarios`
--

INSERT INTO `Usuarios` (`id_usuario`, `correo`, `usuario`, `descripcion`, `imagen`) VALUES
(1, 'maria@example.com', 'María García', 'Me encanta cocinar platos vegetarianos.', 'maria.jpg'),
(2, 'juan@example.com', 'Juan Pérez', 'Amante de las recetas rápidas.', 'juan.jpg'),
(3, 'ana@example.com', 'Ana López', 'Chef aficionada.', 'ana.jpg'),
(13, 'alejandro.moreno.lechado@gmail.com', 'Alesis', 'Hola soy un usuario nuevo', 'https://lh3.googleusercontent.com/a/ACg8ocIj0BuJzuDTWlnxntKf9_CvhBZHTczkPEJH7b0h1E32pFFJTGBG=s96-c'),
(14, 'franciscolozano2005@gmail.com', 'Franxute', 'me cago en tus muertos', 'https://lh3.googleusercontent.com/a/ACg8ocLvBGJcUY-WUbz_QCSerkwDsR70wxyRDwC-csLiWaaeTQz3rUwM6w=s96-c'),
(15, 'tonimansal@gmail.com', 'Antonio Mantas', 'Bombardeiro Crocodilo. un fottuto jacaré volante che vola e bombarda i bambini a gaza e in palestina.\n\nBombardiro Crocodilo é um avião bombardeiro com cabeça de jacaré. Seu hobby é bombardear crianças em Gaza e Palestina.', 'https://lh3.googleusercontent.com/a/ACg8ocIZ_EhaQRDrYBM3cZzRP0M_MsJ9UHnz88gxSKm-dpQJ0JPeo40k=s96-c'),
(16, 'alvaro.guedu@gmail.com', 'saturno2000', 'tonto el que lo lea', 'https://lh3.googleusercontent.com/a/ACg8ocK-ZV6NYgcDMXAcPyqvp9boDzctcf3QFi4_iVsxApEirMvneqsA=s96-c'),
(17, 'jesus@email.com', 'jesus', 'Nuevo usuario registrado', '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `Valoraciones`
--

CREATE TABLE `Valoraciones` (
  `id_valoracion` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `id_receta` int(11) NOT NULL,
  `valoracion` tinyint(4) NOT NULL CHECK (`valoracion` between 1 and 5),
  `id_comentario` int(11) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `Valoraciones`
--

INSERT INTO `Valoraciones` (`id_valoracion`, `id_usuario`, `id_receta`, `valoracion`, `id_comentario`, `fecha`) VALUES
(8, 1, 1, 5, 1, '2025-04-28 08:56:51'),
(9, 2, 2, 4, 2, '2025-04-28 08:56:51'),
(10, 13, 3, 5, NULL, '2025-04-28 08:56:51'),
(11, 13, 2, 4, NULL, '2025-05-25 11:55:11'),
(12, 13, 1, 2, NULL, '2025-05-25 12:21:27'),
(13, 16, 1, 1, NULL, '2025-05-25 15:11:07'),
(14, 16, 2, 1, NULL, '2025-05-25 15:12:06'),
(15, 16, 3, 1, NULL, '2025-05-25 15:12:33'),
(16, 14, 2, 5, NULL, '2025-05-26 14:42:36'),
(17, 14, 1, 3, NULL, '2025-05-26 14:43:14');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `Comentarios`
--
ALTER TABLE `Comentarios`
  ADD PRIMARY KEY (`id_comentario`),
  ADD KEY `id_usuario` (`id_usuario`),
  ADD KEY `id_receta` (`id_receta`);

--
-- Indices de la tabla `Contiene`
--
ALTER TABLE `Contiene`
  ADD PRIMARY KEY (`id_receta`,`id_ingrediente`,`id_um`),
  ADD KEY `id_ingrediente` (`id_ingrediente`),
  ADD KEY `id_um` (`id_um`);

--
-- Indices de la tabla `Guarda`
--
ALTER TABLE `Guarda`
  ADD PRIMARY KEY (`id_usuario`,`id_receta`),
  ADD KEY `id_receta` (`id_receta`);

--
-- Indices de la tabla `Ingredientes`
--
ALTER TABLE `Ingredientes`
  ADD PRIMARY KEY (`id_ingrediente`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `Pasos`
--
ALTER TABLE `Pasos`
  ADD PRIMARY KEY (`id_paso`),
  ADD KEY `id_receta` (`id_receta`);

--
-- Indices de la tabla `Recetas`
--
ALTER TABLE `Recetas`
  ADD PRIMARY KEY (`id_receta`),
  ADD KEY `id_usuario` (`id_usuario`);

--
-- Indices de la tabla `UM`
--
ALTER TABLE `UM`
  ADD PRIMARY KEY (`id_um`),
  ADD UNIQUE KEY `medida` (`medida`);

--
-- Indices de la tabla `Usuarios`
--
ALTER TABLE `Usuarios`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `correo` (`correo`);

--
-- Indices de la tabla `Valoraciones`
--
ALTER TABLE `Valoraciones`
  ADD PRIMARY KEY (`id_valoracion`),
  ADD KEY `fk_valoraciones_recetas` (`id_receta`),
  ADD KEY `fk_valoracion_comentario` (`id_comentario`),
  ADD KEY `fk_valoraciones_usuarios` (`id_usuario`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `Comentarios`
--
ALTER TABLE `Comentarios`
  MODIFY `id_comentario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT de la tabla `Ingredientes`
--
ALTER TABLE `Ingredientes`
  MODIFY `id_ingrediente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `Pasos`
--
ALTER TABLE `Pasos`
  MODIFY `id_paso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `Recetas`
--
ALTER TABLE `Recetas`
  MODIFY `id_receta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `UM`
--
ALTER TABLE `UM`
  MODIFY `id_um` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `Usuarios`
--
ALTER TABLE `Usuarios`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT de la tabla `Valoraciones`
--
ALTER TABLE `Valoraciones`
  MODIFY `id_valoracion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `Comentarios`
--
ALTER TABLE `Comentarios`
  ADD CONSTRAINT `fk_comentarios_recetas` FOREIGN KEY (`id_receta`) REFERENCES `Recetas` (`id_receta`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_comentarios_usuarios` FOREIGN KEY (`id_usuario`) REFERENCES `Usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `Contiene`
--
ALTER TABLE `Contiene`
  ADD CONSTRAINT `FK_Contiene_UM` FOREIGN KEY (`id_um`) REFERENCES `UM` (`id_um`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_contiene_ingredientes` FOREIGN KEY (`id_ingrediente`) REFERENCES `Ingredientes` (`id_ingrediente`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_contiene_recetas` FOREIGN KEY (`id_receta`) REFERENCES `Recetas` (`id_receta`) ON DELETE CASCADE;

--
-- Filtros para la tabla `Guarda`
--
ALTER TABLE `Guarda`
  ADD CONSTRAINT `fk_guarda_recetas` FOREIGN KEY (`id_receta`) REFERENCES `Recetas` (`id_receta`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_guarda_usuarios` FOREIGN KEY (`id_usuario`) REFERENCES `Usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `Pasos`
--
ALTER TABLE `Pasos`
  ADD CONSTRAINT `fk_pasos_recetas` FOREIGN KEY (`id_receta`) REFERENCES `Recetas` (`id_receta`) ON DELETE CASCADE;

--
-- Filtros para la tabla `Recetas`
--
ALTER TABLE `Recetas`
  ADD CONSTRAINT `fk_recetas_usuarios` FOREIGN KEY (`id_usuario`) REFERENCES `Usuarios` (`id_usuario`) ON DELETE CASCADE;

--
-- Filtros para la tabla `Valoraciones`
--
ALTER TABLE `Valoraciones`
  ADD CONSTRAINT `fk_valoracion_comentario` FOREIGN KEY (`id_comentario`) REFERENCES `Comentarios` (`id_comentario`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_valoraciones_recetas` FOREIGN KEY (`id_receta`) REFERENCES `Recetas` (`id_receta`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_valoraciones_usuarios` FOREIGN KEY (`id_usuario`) REFERENCES `Usuarios` (`id_usuario`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
