
--- Region geometry ---
INSERT INTO geometry(type, spatialReference,  createddate, lastmodifieddate, createdby, lastmodifiedby)
VALUES ( 'esriGeometryPoint','wkid: 32628;latestWkid: 32628', CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system');

INSERT INTO coordinate( latitude, longitude, altitude, geometryid, createddate, lastmodifieddate, createdby, lastmodifiedby)
VALUES ( 672, 78.89, 0, 1,CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system' );
--- End region ---

INSERT INTO region (regionid, name, geometryid, createddate, lastmodifieddate, createdby, lastmodifiedby)
VALUES
(1, 'Dakar', 1, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(2, 'Diourbel', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(3, 'Fatick', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(12, 'Kaffrine', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(4, 'Kaolack', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(13, 'Kédougou', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(5, 'Kolda', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(6, 'Louga', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(7, 'Matam', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(8, 'Saint-Louis', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(14, 'Sédhiou', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(9, 'Tambacounda', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(10, 'Thiès', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system'),
(11, 'Ziguinchor', null, CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'system', 'system');