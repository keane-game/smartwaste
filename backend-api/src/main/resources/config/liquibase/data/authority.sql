
DELETE FROM authority;
INSERT INTO authority(authorityid,  createddate, lastmodifieddate, name, createdby, lastmodifiedby,description)
VALUES
(1,  CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'SUPER_ADMIN', 'system', 'system','Super administration'),
(2,  CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'ADMIN', 'system','system', 'administration'),
(3,  CURRENT_TIMESTAMP(2), CURRENT_TIMESTAMP(2), 'USER', 'system','system', 'Possibilité de voir');

-- Faire le mapping authority -> Permissions
INSERT INTO authoritypermission(authorityid, permission)
VALUES
(1, 'USER_VIEW'),
(1, 'ACCESS_ADMIN'),
(1, 'MANAGE_ROLE'),
(1, 'CREATE_USER'),
(2, 'USER_VIEW'),
(2, 'ACCESS_ADMIN'),
(2, 'MANAGE_ROLE'),
(2, 'CREATE_USER'),
(3, 'USER_VIEW');
