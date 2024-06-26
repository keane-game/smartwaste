--- Region geometry ---
INSERT INTO geometry(geometryid,type, spatialReference)
VALUE (1, 'esriGeometryPoint','wkid: 32628;latestWkid: 32628')

INSERT INTO    coodinate(coordinateid, latitude, longitude, altitude, geometryid)
VALUES (1, 672, 78.89, 0, 1);
--- End region ---

--- Department geometry ---
INSERT INTO geometry(geometryid,type, spatialReference)
VALUES (2, 'esriGeometryPoint','{"wkid": 32628;"latestWkid": 32628}')

INSERT INTO coodinate(coordinateid, latitude, longitude, altitude, geometryid)
VALUE (2, 672, 78.89, 0, 1);
--- End Department ---

--- Commune geometry ---
INSERT INTO geometry(geometryid,type, spatialReference)
VALUE (3, 'esriGeometryPolygon','wkid: 32628;latestWkid: 32628')

INSERT INTO    coodinate(coordinateid, latitude, longitude, altitude, geometryid)
VALUE (3, 672, 78.89, 0, 1);
--- End Commune ---
