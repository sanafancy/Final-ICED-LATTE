-- Insertar direcciones
INSERT INTO direccion (id, calle, numero, complemento, codigo_postal, municipio) VALUES (1, 'Calle Falsa', 123, '', 28001, 'Madrid');
INSERT INTO direccion (id, calle, numero, complemento, codigo_postal, municipio) VALUES (2, 'Avenida Siempre Viva', 742, '', 28002, 'Madrid');

-- Insertar clientes
INSERT INTO usuario (dtype, id_usuario, email, pass, nombre, apellidos, dni) VALUES ('Cliente', 1, 'cliente1@ejemplo.com', 'pass123', 'Cliente', 'Uno', '12345678A');
INSERT INTO usuario (dtype, id_usuario, email, pass, nombre, apellidos, dni) VALUES ('Cliente', 2, 'cliente2@ejemplo.com', 'pass123', 'Cliente', 'Dos', '87654321B');

-- Insertar restaurantes
INSERT INTO usuario (dtype, id_usuario, email, pass, nombre, cif, direccion_id) VALUES ('Restaurante', 3, 'restaurante1@ejemplo.com', 'pass123', 'Restaurante A', 'CIF1', 1);
INSERT INTO usuario (dtype, id_usuario, email, pass, nombre, cif, direccion_id) VALUES ('Restaurante', 4, 'restaurante2@ejemplo.com', 'pass123', 'Restaurante B', 'CIF2', 2);

-- Insertar cartas de menú
INSERT INTO carta_menu (id, restaurante_id, nombre) VALUES (1, 3, 'Menú Principal');
INSERT INTO carta_menu (id, restaurante_id, nombre) VALUES (2, 4, 'Menú Especial');

-- Insertar ítems de menú
INSERT INTO item_menu (id, nombre, precio, carta_menu_id) VALUES (1, 'Pizza', 10.0, 1);
INSERT INTO item_menu (id, nombre, precio, carta_menu_id) VALUES (2, 'Hamburguesa', 15.0, 1);
INSERT INTO item_menu (id, nombre, precio, carta_menu_id) VALUES (3, 'Sushi', 20.0, 2);