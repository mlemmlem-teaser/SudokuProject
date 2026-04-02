create database SudokuManager
go

use SudokuManager
go

create table users (
    user_id int primary key,
    gmail varchar(255) not null unique,
    password varchar(255) not null,
    username varchar(100) not null
)

create table levels (
    level_id int primary key,
    size int not null,
    board_data varchar(256) not null
)

create table progress (
    user_id int not null,
    level_id int not null,
    completed bit default 0,
    primary key (user_id, level_id),
    foreign key (user_id) references users(user_id),
    foreign key (level_id) references levels(level_id)
)

create table SudokuTime (
    id int primary key identity(1,1),
    player_name varchar(50),
    level varchar(20),
    time_seconds int,
    played_at datetime default getdate()
)

insert into users values
(60001,'example01@gmail.com','Password123','User60001'),
(60002,'example02@gmail.com','Password234','User60002'),
(60003,'example03@gmail.com','Password345','User60003'),
(60004,'example04@gmail.com','Password456','User60004'),
(60005,'example05@gmail.com','Password567','User60005'),
(60006,'example06@gmail.com','Password678','User60006'),
(60007,'example07@gmail.com','Password789','User60007'),
(60008,'example08@gmail.com','Password890','User60008'),
(60009,'example09@gmail.com','Password901','User60009'),
(60010,'example@10gmail.com','Password012','User60010')

insert into levels values
(10001,9,'530070000600195000098000060800060003400803001700020006060000280000419005000080079'),
(10002,4,'1234341221434321'),
(10003,16,'123456789ABCDEFG56789ABCDEFG12349ABCDEFG12345678DEFG123456789ABC23456789ABCDEFG16789ABCDEFG12345ABCDEFG123456789EFG123456789ABCD3456789ABCDEFG12789ABCDEFG123456BCDEFG123456789AFG123456789ABCDE456789ABCDEFG12389ABCDEFG1234567CDEFG123456789ABG123456789ABCDEF'),
(10004,9,'600120384008459072000006005000264030070080006940003000310000050089700000502000190'),
(10005,9,'302609005000000900000050030200000040000030000050000003070090000006000000400701208'),
(10006,4,'4321123443211234'),
(10007,9,'200080300060070084030500209000105408000000000402706000301007040720040060004010003'),
(10008,9,'000260701680070090190004500820100040004602900050003028009300074040050036703018000'),
(10009,4,'2143431221344213'),
(10010,9,'005300000800000020070010500400005300010070006003200080060500009004000030000009700')

insert into progress values
(60001,10001,1),
(60001,10002,0),
(60002,10003,1),
(60003,10004,0),
(60004,10005,1),
(60005,10006,0),
(60006,10007,1),
(60007,10008,0),
(60008,10009,1),
(60009,10010,0)
