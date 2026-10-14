-- La entidad ApiKey declara el id como Long (BIGINT), pero V10 creó la columna como SERIAL (INTEGER).
-- Con spring.jpa.hibernate.ddl-auto=validate (perfil por defecto) Hibernate rechaza el arranque con
-- "wrong column type encountered in column [id] in table [api_keys]; found [serial (Types#INTEGER)],
-- but expecting [bigint]". El perfil prod usa ddl-auto=none, por lo que el desajuste no se notaba allí.
-- Ensanchar INTEGER a BIGINT conserva los valores existentes.
ALTER TABLE api_keys ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE api_keys_id_seq AS BIGINT;
