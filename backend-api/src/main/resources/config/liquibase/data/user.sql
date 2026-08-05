
INSERT INTO users(userid,authorityid,useremail,userpassword,userfirstname,userlastname,activated,archived,createdby,lastmodifiedby,createddate,lastmodifieddate)
VALUES
(1,1,'admin@gmail.com','$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC','Administrator','Administrator','true','false','system','system',CURRENT_TIMESTAMP(2),CURRENT_TIMESTAMP(2)),
(2,2,'user@gmail.com','$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K','User','User','true','true','system','system',CURRENT_TIMESTAMP(2),CURRENT_TIMESTAMP(2));
