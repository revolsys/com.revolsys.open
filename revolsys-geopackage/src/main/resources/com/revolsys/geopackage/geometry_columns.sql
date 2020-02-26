CREATE VIEW geometry_columns AS
  SELECT
    table_name AS f_table_name,
    column_name AS f_geometry_column,
    code4name (geometry_type_name) AS geometry_type,
    2 + (CASE z WHEN 1 THEN 1 WHEN 2 THEN 1 ELSE 0 END) + (CASE m WHEN 1 THEN 1 WHEN 2 THEN 1 ELSE 0 END) AS coord_dimension,
    srs_id AS srid
  FROM gpkg_geometry_columns;
