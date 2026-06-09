-- =============================================
-- 占星顧問後台系統 — 初始化 Schema  (v13)
-- =============================================

-- 代碼對照表（先建，其他表邏輯上依賴它）---
CREATE TABLE IF NOT EXISTS code_references (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(10) NOT NULL UNIQUE,
    category    VARCHAR(20) NOT NULL CHECK (category IN ('planet', 'sign', 'aspect')),
    zh_name     VARCHAR(50) NOT NULL
);

INSERT INTO code_references (code, category, zh_name) VALUES
('Q',  'planet',   '太陽'),
('W',  'planet',   '月亮'),
('E',  'planet',   '水星'),
('R',  'planet',   '金星'),
('T',  'planet',   '火星'),
('Y',  'planet',   '木星'),
('U',  'planet',   '土星'),
('I',  'planet',   '天王星'),
('O',  'planet',   '海王星'),
('P',  'planet',   '冥王星'),
('‹',  'planet',   '北交點'),
('Z',  'planet',   'ASC'),
('X',  'planet',   'MC'),
('a',  'sign',     '牡羊座'),
('s',  'sign',     '金牛座'),
('d',  'sign',     '雙子座'),
('f',  'sign',     '巨蟹座'),
('g',  'sign',     '獅子座'),
('h',  'sign',     '處女座'),
('j',  'sign',     '天秤座'),
('k',  'sign',     '天蠍座'),
('l',  'sign',     '射手座'),
('z',  'sign',     '摩羯座'),
('x',  'sign',     '水瓶座'),
('c',  'sign',     '雙魚座'),
('q',  'aspect',   '合相'),
('t',  'aspect',   '六分相'),
('r',  'aspect',   '四分相'),
('e',  'aspect',   '三分相'),
('w',  'aspect',   '對分相')
ON CONFLICT (code) DO NOTHING;

-- 1. 客戶基本資訊
CREATE TABLE IF NOT EXISTS clients (
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    birth_date        DATE,
    birth_time        TIME,
    birth_place       VARCHAR(200),
    chart_image_path  VARCHAR(500),
    asc_sign          VARCHAR(10),
    asc_degree_num    SMALLINT CHECK (asc_degree_num BETWEEN 0 AND 29),
    asc_minute_num    SMALLINT CHECK (asc_minute_num BETWEEN 0 AND 59),
    mc_sign           VARCHAR(10),
    mc_degree_num     SMALLINT CHECK (mc_degree_num BETWEEN 0 AND 29),
    mc_minute_num     SMALLINT CHECK (mc_minute_num BETWEEN 0 AND 59),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

-- 2. 行星位置
CREATE TABLE IF NOT EXISTS planet_positions (
    id            SERIAL PRIMARY KEY,
    client_id     INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    planet        VARCHAR(10),
    sign          VARCHAR(10),
    degree_num    SMALLINT CHECK (degree_num BETWEEN 0 AND 29),
    minute_num    SMALLINT CHECK (minute_num BETWEEN 0 AND 59),
    house         INTEGER CHECK (house BETWEEN 1 AND 12),
    is_retrograde BOOLEAN NOT NULL DEFAULT FALSE,
    notes         VARCHAR(200),
    is_lord       BOOLEAN NOT NULL DEFAULT FALSE
);

-- 3. 宮位守護星
CREATE TABLE IF NOT EXISTS house_rulers (
    id             SERIAL PRIMARY KEY,
    client_id      INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    house_number   INTEGER NOT NULL CHECK (house_number BETWEEN 1 AND 12),
    ruling_planet  VARCHAR(10),
    flies_to_sign  VARCHAR(10),
    flies_to_house INTEGER CHECK (flies_to_house BETWEEN 1 AND 12)
);

-- 4. 重要相位
CREATE TABLE IF NOT EXISTS aspects (
    id           SERIAL PRIMARY KEY,
    client_id    INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    planet1      VARCHAR(10),
    aspect_type  VARCHAR(10),
    planet2      VARCHAR(10),
    orb          DECIMAL(4,2),
    notes        TEXT
);

-- 5. 我的解析
CREATE TABLE IF NOT EXISTS analysis_notes (
    id          SERIAL PRIMARY KEY,
    client_id   INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    title       VARCHAR(200),
    content     TEXT,
    sort_order  INTEGER DEFAULT 0,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 6. 諮詢記錄
CREATE TABLE IF NOT EXISTS consultation_logs (
    id                SERIAL PRIMARY KEY,
    client_id         INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    consultation_date TIMESTAMP,
    notes             TEXT,
    created_at        TIMESTAMP DEFAULT NOW()
);

-- 7. 備份記錄
CREATE TABLE IF NOT EXISTS backup_records (
    id           SERIAL PRIMARY KEY,
    file_path    VARCHAR(500) NOT NULL,
    note         VARCHAR(100) DEFAULT '手動備份',
    created_at   TIMESTAMP DEFAULT NOW()
);

-- 8. 星盤設定
CREATE TABLE IF NOT EXISTS chart_preferences (
    id                    SERIAL PRIMARY KEY,
    orb_conjunction       DECIMAL(3,1) DEFAULT 8.0,   -- 合相
    orb_opposition        DECIMAL(3,1) DEFAULT 8.0,   -- 對分相
    orb_trine             DECIMAL(3,1) DEFAULT 8.0,   -- 三分相
    orb_square            DECIMAL(3,1) DEFAULT 6.0,   -- 四分相
    orb_sextile           DECIMAL(3,1) DEFAULT 6.0    -- 六分相
);

INSERT INTO chart_preferences (id) VALUES (1);