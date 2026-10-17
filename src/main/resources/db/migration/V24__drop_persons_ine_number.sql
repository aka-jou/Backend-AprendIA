-- El número de INE ya no forma parte del registro de usuarios (observación del frontend sobre el
-- formulario "agregar personas"): venía de la base de datos anterior y no se utiliza.
-- Se elimina la columna agregada en V19. Los valores existentes se pierden (eran datos de prueba).
ALTER TABLE persons DROP COLUMN IF EXISTS ine_number;
