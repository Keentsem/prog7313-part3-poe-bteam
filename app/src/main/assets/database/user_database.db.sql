-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    name TEXT NOT NULL DEFAULT ''
);

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    monthlyAmount REAL NOT NULL DEFAULT 0.0,
    iconType TEXT NOT NULL,
    icon TEXT,
    color INTEGER
);

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    balance REAL NOT NULL,
    type TEXT NOT NULL
);

-- Create expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    amount REAL NOT NULL,
    categoryId INTEGER NOT NULL,
    description TEXT,
    photoUri TEXT,
    startDate TEXT NOT NULL,
    endDate TEXT NOT NULL,
    date INTEGER NOT NULL,
    FOREIGN KEY (categoryId) REFERENCES categories (id) ON DELETE CASCADE
);

-- Create budget_goals table
CREATE TABLE IF NOT EXISTS budget_goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    amount REAL NOT NULL,
    current_amount REAL NOT NULL DEFAULT 0.0,
    description TEXT,
    target_date INTEGER NOT NULL
);

-- Create savings_goals table
CREATE TABLE IF NOT EXISTS savings_goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    target_amount REAL NOT NULL,
    current_amount REAL NOT NULL DEFAULT 0.0,
    target_date INTEGER NOT NULL,
    description TEXT
); 