-- Create Users table
CREATE TABLE sdev_users (
  user_id INTEGER PRIMARY KEY,
  email  VARCHAR(75) NOT NULL UNIQUE,
  firstname VARCHAR(50) NOT NULL,
  lastname VARCHAR(75) NOT NULL,
  city VARCHAR(75),
  State CHAR(2),
  zip VARCHAR(10) 
);

-- Roles table
CREATE TABLE roles (
  role_id INTEGER PRIMARY KEY,
  role varchar(20) NOT NULL UNIQUE
);

-- user-info
CREATE TABLE user_info (
  user_id INTEGER Primary Key, 
  password  VARCHAR(100)  NOT NULL,
  CONSTRAINT fk_wu2 Foreign Key (user_id) 
  references  sdev_users(user_id) on delete cascade   
);


-- User roles
CREATE TABLE user_roles (
  user_id INTEGER NOT NULL,
  role_id INTEGER NOT NULL,
  Constraint PKUR primary key (user_id, role_id),
  Constraint fk_ur1 Foreign Key (user_id) references  
   sdev_users(user_id) on delete cascade,   
  Constraint fk_ur2 Foreign Key (role_id) references  
   roles(role_id) on delete cascade    
);

-- Account data
CREATE TABLE CustomerAccount (
  account_id INTEGER Primary Key,
  user_id INTEGER NOT NULL references sdev_users (user_id),
  Cardholdername VARCHAR(75) NOT NULL,
  CardType VARCHAR(20) NOT NULL,
  ServiceCode VARCHAR(20) NOT NULL,
  CardNumber VARCHAR(30) NOT NULL,
  expiredate date NOT NULL
);

-- Login data
CREATE TABLE LoginInfo (
  user_id INTEGER NOT NULL Primary Key references sdev_users (user_id),
  lastAttempt TIMESTAMP,
  numAttempt INTEGER,
  lastSuccess TIMESTAMP
);

-- Initialize login data
insert into LoginInfo (user_id, lastAttempt, numAttempt, lastSuccess)
values (1,'2018-05-06 14:23:13.309',0,'2018-05-06 14:23:13.309');

-- Insert records
insert into sdev_users (user_id, email, firstname, lastname,
city, state, zip)
values (1,'james.robertson@umuc.edu','Jim', 'Robertson','Adelphi',
'MD','20706');

--Insert user_info
insert into user_info (user_id, password) 
values (1,'nY1M/cx+b2rmgCYfx6LRDQ==');

-- Insert roles
insert into roles (role_id, role)
values (1,'Customer');

insert into roles (role_id, role)
values (2,'Admin');


-- Insert user_roles
insert into user_roles (user_id, role_id)
values (1,1);

-- Insert CustomerAccount
insert into CustomerAccount (account_id, user_id,
CardType, ServiceCode, CardNumber, Cardholdername, expiredate)
values (1,1,'MasterCard','27aD','1111111111111','James Robertson','02/23/2016');
