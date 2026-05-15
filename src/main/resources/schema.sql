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

-- 8. 星盤偏好設定（V2：全域單筆，系統啟動時 INSERT 預設值）
CREATE TABLE IF NOT EXISTS chart_preferences (
    id                    SERIAL PRIMARY KEY,

    -- 相位容許度（單位：度）
    orb_conjunction       DECIMAL(4,1) DEFAULT 8.0,   -- 合相
    orb_sextile           DECIMAL(4,1) DEFAULT 6.0,   -- 六分相
    orb_square            DECIMAL(4,1) DEFAULT 8.0,   -- 刑相（四分）
    orb_trine             DECIMAL(4,1) DEFAULT 8.0,   -- 三分相
    orb_opposition        DECIMAL(4,1) DEFAULT 8.0,   -- 對分相
    orb_semi_sextile      DECIMAL(4,1) DEFAULT 2.0,   -- 十二分相
    orb_semi_square       DECIMAL(4,1) DEFAULT 2.0,   -- 八分相
    orb_quintile          DECIMAL(4,1) DEFAULT 2.0,   -- 五分相
    orb_sesquiquadrate    DECIMAL(4,1) DEFAULT 2.0,   -- 倍半四分相
    orb_quincunx          DECIMAL(4,1) DEFAULT 3.0,   -- 補十二分相（150°）

    -- 相位顯示開關（主要相位預設開；次要相位預設關）
    show_conjunction      BOOLEAN DEFAULT TRUE,
    show_sextile          BOOLEAN DEFAULT TRUE,
    show_square           BOOLEAN DEFAULT TRUE,
    show_trine            BOOLEAN DEFAULT TRUE,
    show_opposition       BOOLEAN DEFAULT TRUE,
    show_semi_sextile     BOOLEAN DEFAULT FALSE,
    show_semi_square      BOOLEAN DEFAULT FALSE,
    show_quintile         BOOLEAN DEFAULT FALSE,
    show_sesquiquadrate   BOOLEAN DEFAULT FALSE,
    show_quincunx         BOOLEAN DEFAULT FALSE,

    -- 嚴謹模式（開啟後使用固定較小容許度，定義於後端常數，不存入 DB）
    strict_mode           BOOLEAN DEFAULT FALSE,

    -- 行星個別容許度（僅日月水金火木土；外行星與小天體不設個別值）
    orb_sun               DECIMAL(4,1) DEFAULT 8.0,
    orb_moon              DECIMAL(4,1) DEFAULT 8.0,
    orb_mercury           DECIMAL(4,1) DEFAULT 6.0,
    orb_venus             DECIMAL(4,1) DEFAULT 6.0,
    orb_mars              DECIMAL(4,1) DEFAULT 6.0,
    orb_jupiter           DECIMAL(4,1) DEFAULT 5.0,
    orb_saturn            DECIMAL(4,1) DEFAULT 5.0,

    -- 外行星 / 軸點顯示開關（預設開）
    show_uranus           BOOLEAN DEFAULT TRUE,
    show_neptune          BOOLEAN DEFAULT TRUE,
    show_pluto            BOOLEAN DEFAULT TRUE,
    show_asc              BOOLEAN DEFAULT TRUE,   -- 上升點
    show_mc               BOOLEAN DEFAULT TRUE,   -- 天頂

    -- 小天體顯示開關（預設關；待位置資料擴充後生效）
    show_chiron           BOOLEAN DEFAULT FALSE,  -- 凱龍星
    show_ceres            BOOLEAN DEFAULT FALSE,  -- 穀神星
    show_pallas           BOOLEAN DEFAULT FALSE,  -- 智神星
    show_juno             BOOLEAN DEFAULT FALSE,  -- 婚神星
    show_vesta            BOOLEAN DEFAULT FALSE,  -- 灶神星
    show_north_node       BOOLEAN DEFAULT FALSE,  -- 北交點
    show_south_node       BOOLEAN DEFAULT FALSE,  -- 南交點
    show_lilith           BOOLEAN DEFAULT FALSE,  -- 莉莉絲（黑月）
    show_pof              BOOLEAN DEFAULT FALSE,  -- 幸運點（Part of Fortune）
    show_vertex           BOOLEAN DEFAULT FALSE,  -- 宿命點
    show_east_point       BOOLEAN DEFAULT FALSE,  -- 東昇點
    show_dsc              BOOLEAN DEFAULT FALSE,  -- 下降點（備用）
    show_ic               BOOLEAN DEFAULT FALSE,  -- 天底（備用）

    updated_at            TIMESTAMP DEFAULT NOW()
);

-- 插入唯一一筆預設設定（系統只有一位使用者，id 永遠為 1）
INSERT INTO chart_preferences DEFAULT VALUES;
