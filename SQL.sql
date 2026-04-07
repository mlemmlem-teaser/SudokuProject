CREATE DATABASE SudokuManager;
GO
USE SudokuManager;
GO

-- Vai trò người dùng: admin được quản lý puzzle, user chỉ được chơi.
CREATE TABLE roles (
    role_id INT PRIMARY KEY,
    role_name VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO roles (role_id, role_name) VALUES
(1, 'admin'),
(2, 'user');

-- Bảng tài khoản.
-- last_solve_at dùng để giới hạn mỗi user chỉ được dùng Solve 1 lần / 10 phút.
CREATE TABLE users (
    user_id INT PRIMARY KEY,
    gmail VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    role_id INT NOT NULL,
    last_solve_at DATETIME NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);
INSERT INTO users (user_id, gmail, password, username, role_id, last_solve_at) VALUES
(60001, 'admin@gmail.com',     'Admin123',    'Admin',     1, NULL),
(60002, 'linh@example.com',    'Password234', 'Linh',      2, NULL),
(60003, 'khoa@example.com',    'Password345', 'Khoa',      2, NULL),
(60004, 'mai@example.com',     'Password456', 'Mai',       2, NULL),
(60005, 'nam@example.com',     'Password567', 'Nam',       2, NULL),
(60006, 'hieu@example.com',    'Password678', 'Hieu',      2, NULL),
(60007, 'thao@example.com',    'Password789', 'Thao',      2, NULL),
(60008, 'quang@example.com',   'Password890', 'Quang',     2, NULL),
(60009, 'an@example.com',      'Password901', 'An',        2, NULL),
(60010, 'my@example.com',      'Password012', 'My',        2, NULL);

-- Bảng puzzle.
-- title và difficulty giúp dữ liệu đa dạng hơn.
CREATE TABLE levels (
    level_id INT PRIMARY KEY,
    size INT NOT NULL,
    board_data VARCHAR(1024) NOT NULL,
    title NVARCHAR(150) NULL,
    difficulty VARCHAR(20) NULL,
    created_by INT NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

INSERT INTO levels (level_id, size, board_data, title, difficulty, created_by) VALUES
(10001, 9,  '530070000600195000098000060800060003400803001700020006060000280000419005000080079', 'Starter 9x9', 'Easy', 60001),
(10002, 4,  '1234341221434321', 'Mini 4x4 A', 'Easy', 60001),
(10003, 16, '123456789ABCDEFG56789ABCDEFG12349ABCDEFG12345678DEFG123456789ABC23456789ABCDEFG16789ABCDEFG12345ABCDEFG123456789EFG123456789ABCD3456789ABCDEFG12789ABCDEFG123456BCDEFG123456789AFG123456789ABCDE456789ABCDEFG12389ABCDEFG1234567CDEFG123456789ABG123456789ABCDEF', 'Monster 16x16', 'Hard', 60001),
(10004, 9,  '600120384008459072000006005000264030070080006940003000310000050089700000502000190', 'Daily 9x9 A', 'Medium', 60001),
(10005, 9,  '302609005000000900000050030200000040000030000050000003070090000006000000400701208', 'Daily 9x9 B', 'Hard', 60001),
(10006, 4,  '4321123443211234', 'Mini 4x4 B', 'Easy', 60001),
(10007, 9,  '200080300060070084030500209000105408000000000402706000301007040720040060004010003', 'Training 9x9 A', 'Medium', 60001),
(10008, 9,  '000260701680070090190004500820100040004602900050003028009300074040050036703018000', 'Training 9x9 B', 'Medium', 60001),
(10009, 4,  '2143431221344213', 'Mini 4x4 C', 'Hard', 60001),
(10010, 9,  '005300000800000020070010500400005300010070006003200080060500009004000030000009700', 'Challenge 9x9 A', 'Hard', 60001);

-- Trạng thái hoàn thành của từng user cho từng level.
CREATE TABLE progress (
    user_id INT NOT NULL,
    level_id INT NOT NULL,
    completed BIT NOT NULL DEFAULT 0,
    completed_at DATETIME NULL,
    PRIMARY KEY (user_id, level_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (level_id) REFERENCES levels(level_id) ON DELETE CASCADE
);

INSERT INTO progress (user_id, level_id, completed, completed_at) VALUES
(60002, 10001, 1, GETDATE()),
(60002, 10002, 0, NULL),
(60003, 10004, 1, GETDATE()),
(60004, 10005, 1, GETDATE()),
(60005, 10006, 0, NULL),
(60006, 10007, 1, GETDATE()),
(60007, 10008, 0, NULL),
(60008, 10009, 1, GETDATE()),
(60009, 10010, 0, NULL);

-- Best time theo từng user và từng màn.
-- Mỗi user chỉ có 1 dòng cho mỗi level.
CREATE TABLE SudokuTime (
    user_id INT NOT NULL,
    level_id INT NOT NULL,
    best_seconds INT NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT GETDATE(),
    PRIMARY KEY (user_id, level_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (level_id) REFERENCES levels(level_id) ON DELETE CASCADE
);

INSERT INTO SudokuTime (user_id, level_id, best_seconds, updated_at) VALUES
(60002, 10001, 305, GETDATE()),
(60002, 10002, 74, GETDATE()),
(60003, 10004, 412, GETDATE()),
(60004, 10005, 580, GETDATE()),
(60006, 10007, 401, GETDATE()),
(60008, 10009, 88, GETDATE());
