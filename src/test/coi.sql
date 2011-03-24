
CREATE TABLE customers 
(
  cid int NOT NULL auto_increment, 
  PRIMARY KEY(cid),
  name varchar(32) NOT NULL,
  KEY(name)
) engine=akibandb;

CREATE TABLE orders
(
  oid int NOT NULL auto_increment, 
  PRIMARY KEY(oid),
  cid int NOT NULL,
  order_date date NOT NULL,
  KEY(order_date),
  CONSTRAINT `__akiban_fk_0` FOREIGN KEY `__akiban_fk_0` (cid) REFERENCES customers(cid)
) engine=akibandb;

CREATE TABLE items
(
  iid int NOT NULL auto_increment, 
  PRIMARY KEY(iid),
  oid int NOT NULL,
  sku varchar(32) NOT NULL,
  KEY(sku),
  quan int NOT NULL,
  CONSTRAINT `__akiban_fk_1` FOREIGN KEY `__akiban_fk_1` (oid) REFERENCES orders(oid)
) engine=akibandb;

INSERT INTO customers(cid,name) VALUES(1,'Smith');
INSERT INTO customers(cid,name) VALUES(2,'Jones');

INSERT INTO orders(oid,cid,order_date) VALUES(101, 1, '2011-03-01');
INSERT INTO orders(oid,cid,order_date) VALUES(102, 1, '2011-03-02');
INSERT INTO orders(oid,cid,order_date) VALUES(201, 2, '2011-03-03');

INSERT INTO items(oid,sku,quan) VALUES(101,'1234',100);
INSERT INTO items(oid,sku,quan) VALUES(101,'4567',50);
INSERT INTO items(oid,sku,quan) VALUES(201,'9876',1);
