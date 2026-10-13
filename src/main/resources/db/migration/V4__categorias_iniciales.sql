-- Categorias minimas para registrar productos desde PharmaMobile en una BD nueva.
insert into categorias (nombre, descripcion, estado, fecha_creacion)
values ('Analgesicos', 'Medicamentos para el dolor', true, systimestamp);

insert into categorias (nombre, descripcion, estado, fecha_creacion)
values ('Antibioticos', 'Medicamentos antibacterianos', true, systimestamp);

insert into categorias (nombre, descripcion, estado, fecha_creacion)
values ('Vitaminas', 'Suplementos vitaminicos', true, systimestamp);
