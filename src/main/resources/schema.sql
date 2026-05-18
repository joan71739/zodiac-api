-- =============================================
-- 占星顧問後台系統 — 初始化 Schema  (v9)
-- =============================================

-- 1. 客戶基本資訊
CREATE TABLE IF NOT EXISTS clients (
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    birth_date        DATE,
    birth_time        TIME,
    birth_place       VARCHAR(200),
    chart_image_path  VARCHAR(500),
    -- v9（V2 前置）：上升 / 天頂四軸資訊（允許 NULL）
    asc_sign          VARCHAR(50),
    asc_degree_num    SMALLINT CHECK (asc_degree_num BETWEEN 0 AND 29),
    asc_minute_num    SMALLINT CHECK (asc_minute_num BETWEEN 0 AND 59),
    mc_sign           VARCHAR(50),
    mc_degree_num     SMALLINT CHECK (mc_degree_num BETWEEN 0 AND 29),
    mc_minute_num     SMALLINT CHECK (mc_minute_num BETWEEN 0 AND 59),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

-- 2. 行星位置
CREATE TABLE IF NOT EXISTS planet_positions (
    id           SERIAL PRIMARY KEY,
    client_id    INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    planet       VARCHAR(50),   -- 太陽 / 月亮 / 水星 / 金星 / 火星 /
                                -- 木星 / 土星 / 天王星 / 海王星 /
                                -- 冥王星 / 凱龍星
                                -- (v8 起：命主星改為 is_lord flag，不再是獨立列)
    sign         VARCHAR(50),   -- 12 星座
    degree_num   SMALLINT CHECK (degree_num BETWEEN 0 AND 29),  -- 度 0~29
    minute_num   SMALLINT CHECK (minute_num BETWEEN 0 AND 59),  -- 分 0~59
    house        INTEGER CHECK (house BETWEEN 1 AND 12),
    notes        VARCHAR(200),
    is_lord      BOOLEAN NOT NULL DEFAULT FALSE                  -- v8 新增：是否為命主星
);

-- 3. 宮位守護星
CREATE TABLE IF NOT EXISTS house_rulers (
    id             SERIAL PRIMARY KEY,
    client_id      INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    house_number   INTEGER NOT NULL CHECK (house_number BETWEEN 1 AND 12),
    ruling_planet  VARCHAR(50),
    flies_to_sign  VARCHAR(50),
    flies_to_house INTEGER CHECK (flies_to_house BETWEEN 1 AND 12)
);

-- 4. 重要相位
CREATE TABLE IF NOT EXISTS aspects (
    id           SERIAL PRIMARY KEY,
    client_id    INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    planet1      VARCHAR(50),
    aspect_type  VARCHAR(30),   -- CONJUNCTION / SEXTILE / SQUARE / TRINE / OPPOSITION
    planet2      VARCHAR(50),
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
