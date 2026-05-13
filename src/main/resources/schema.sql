-- =============================================
-- 占星顧問後台系統 — 初始化 Schema  (v8)
-- =============================================

CREATE TABLE IF NOT EXISTS clients (
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    birth_date        DATE,
    birth_time        TIME,
    birth_place       VARCHAR(200),
    chart_image_path  VARCHAR(500),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS planet_positions (
    id           SERIAL PRIMARY KEY,
    client_id    INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    planet       VARCHAR(50),   -- 太陽 / 月亮 / 水星 / 金星 / 火星 /
                                -- 木星 / 土星 / 天王星 / 海王星 /
                                -- 冥王星 / 凱龍星
                                -- (v8: 命主星改為 is_lord checkbox，不再是獨立列)
    sign         VARCHAR(50),   -- 12星座
    degree_num   SMALLINT CHECK (degree_num BETWEEN 0 AND 29),  -- 度 0~29
    minute_num   SMALLINT CHECK (minute_num BETWEEN 0 AND 59),  -- 分 0~59
    house        INTEGER CHECK (house BETWEEN 1 AND 12),
    notes        VARCHAR(200),
    is_lord      BOOLEAN NOT NULL DEFAULT FALSE                  -- v8 新增：是否為命主星
);

CREATE TABLE IF NOT EXISTS house_rulers (
    id             SERIAL PRIMARY KEY,
    client_id      INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    house_number   INTEGER NOT NULL CHECK (house_number BETWEEN 1 AND 12),
    ruling_planet  VARCHAR(50),
    flies_to_sign  VARCHAR(50),
    flies_to_house INTEGER CHECK (flies_to_house BETWEEN 1 AND 12)
);

CREATE TABLE IF NOT EXISTS aspects (
    id           SERIAL PRIMARY KEY,
    client_id    INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    planet1      VARCHAR(50),
    aspect_type  VARCHAR(30),   -- CONJUNCTION / SEXTILE / SQUARE / TRINE / OPPOSITION
    planet2      VARCHAR(50),
    orb          DECIMAL(4,2),
    notes        TEXT
);

CREATE TABLE IF NOT EXISTS analysis_notes (
    id          SERIAL PRIMARY KEY,
    client_id   INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    title       VARCHAR(200),
    content     TEXT,
    sort_order  INTEGER DEFAULT 0,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS consultation_logs (
    id                SERIAL PRIMARY KEY,
    client_id         INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    consultation_date TIMESTAMP,
    notes             TEXT,
    created_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS backup_records (
    id           SERIAL PRIMARY KEY,
    file_path    VARCHAR(500) NOT NULL,
    note         VARCHAR(100) DEFAULT '手動備份',
    created_at   TIMESTAMP DEFAULT NOW()
);

-- =============================================
-- 既有資料庫升級用（已上線環境執行這段）
-- =============================================
-- ALTER TABLE planet_positions ADD COLUMN IF NOT EXISTS is_lord BOOLEAN NOT NULL DEFAULT FALSE;
-- DELETE FROM planet_positions WHERE planet = '命主星';
