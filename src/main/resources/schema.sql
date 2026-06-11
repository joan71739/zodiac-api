-- =============================================
-- 占星顧問後台系統 — 初始化 Schema  (v14)
-- =============================================

-- 代碼對照表
CREATE TABLE IF NOT EXISTS code_references (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(10) NOT NULL UNIQUE,
    category    VARCHAR(20) NOT NULL CHECK (category IN ('planet', 'sign', 'aspect')),
    zh_name     VARCHAR(50) NOT NULL
);

COMMENT ON TABLE  code_references          IS '代碼對照表，統一管理行星、星座、相位的代碼與中文名稱';
COMMENT ON COLUMN code_references.id       IS '流水號';
COMMENT ON COLUMN code_references.code     IS '代碼（唯一值），例如 Q=太陽、a=牡羊座、q=合相';
COMMENT ON COLUMN code_references.category IS '分類：planet=行星、sign=星座、aspect=相位';
COMMENT ON COLUMN code_references.zh_name  IS '中文名稱，例如 太陽、牡羊座、合相';

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

COMMENT ON TABLE  clients                  IS '客戶基本資訊';
COMMENT ON COLUMN clients.id               IS '流水號';
COMMENT ON COLUMN clients.name             IS '客戶姓名';
COMMENT ON COLUMN clients.birth_date       IS '出生日期';
COMMENT ON COLUMN clients.birth_time       IS '出生時間';
COMMENT ON COLUMN clients.birth_place      IS '出生地點';
COMMENT ON COLUMN clients.chart_image_path IS '星盤圖片路徑';
COMMENT ON COLUMN clients.asc_sign         IS '上升星座代碼，參考 code_references（category=sign）';
COMMENT ON COLUMN clients.asc_degree_num   IS '上升度數（0~29）';
COMMENT ON COLUMN clients.asc_minute_num   IS '上升分數（0~59）';
COMMENT ON COLUMN clients.mc_sign          IS '天頂星座代碼，參考 code_references（category=sign）';
COMMENT ON COLUMN clients.mc_degree_num    IS '天頂度數（0~29）';
COMMENT ON COLUMN clients.mc_minute_num    IS '天頂分數（0~59）';
COMMENT ON COLUMN clients.created_at       IS '建立時間';
COMMENT ON COLUMN clients.updated_at       IS '最後更新時間';

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

COMMENT ON TABLE  planet_positions               IS '客戶命盤行星位置';
COMMENT ON COLUMN planet_positions.id            IS '流水號';
COMMENT ON COLUMN planet_positions.client_id     IS '對應客戶 id';
COMMENT ON COLUMN planet_positions.planet        IS '行星代碼，參考 code_references（category=planet）';
COMMENT ON COLUMN planet_positions.sign          IS '所在星座代碼，參考 code_references（category=sign）';
COMMENT ON COLUMN planet_positions.degree_num    IS '度數（0~29）';
COMMENT ON COLUMN planet_positions.minute_num    IS '分數（0~59）';
COMMENT ON COLUMN planet_positions.house         IS '所在宮位（1~12）';
COMMENT ON COLUMN planet_positions.is_retrograde IS '是否逆行';
COMMENT ON COLUMN planet_positions.notes         IS '備註';
COMMENT ON COLUMN planet_positions.is_lord       IS '是否為命主星（同一客戶只有一列為 TRUE）';

-- 3. 宮位守護星
CREATE TABLE IF NOT EXISTS house_rulers (
    id             SERIAL PRIMARY KEY,
    client_id      INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    house_number   INTEGER NOT NULL CHECK (house_number BETWEEN 1 AND 12),
    ruling_planet  VARCHAR(10),
    flies_to_house INTEGER CHECK (flies_to_house BETWEEN 1 AND 12)
    flies_to_sign  VARCHAR(10),
);

COMMENT ON TABLE  house_rulers               IS '各宮位守護星及其飛入位置';
COMMENT ON COLUMN house_rulers.id            IS '流水號';
COMMENT ON COLUMN house_rulers.client_id     IS '對應客戶 id';
COMMENT ON COLUMN house_rulers.house_number  IS '宮位編號（1~12）';
COMMENT ON COLUMN house_rulers.ruling_planet IS '守護星代碼，參考 code_references（category=planet）';
COMMENT ON COLUMN house_rulers.flies_to_house IS '守護星飛入的宮位（1~12）';
COMMENT ON COLUMN house_rulers.flies_to_sign IS '守護星飛入的星座代碼，參考 code_references（category=sign）';

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

COMMENT ON TABLE  aspects             IS '行星間的重要相位，單向儲存（A與B有相位只存一筆），查詢時需同時比對 planet1 和 planet2';
COMMENT ON COLUMN aspects.id          IS '流水號';
COMMENT ON COLUMN aspects.client_id   IS '對應客戶 id';
COMMENT ON COLUMN aspects.planet1     IS '行星代碼，參考 code_references（category=planet）';
COMMENT ON COLUMN aspects.aspect_type IS '相位代碼，參考 code_references（category=aspect）';
COMMENT ON COLUMN aspects.planet2     IS '行星代碼，參考 code_references（category=planet）';
COMMENT ON COLUMN aspects.orb         IS '容許度（度數）';
COMMENT ON COLUMN aspects.notes       IS '備註';

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

COMMENT ON TABLE  analysis_notes            IS '針對客戶命盤的解析筆記';
COMMENT ON COLUMN analysis_notes.id         IS '流水號';
COMMENT ON COLUMN analysis_notes.client_id  IS '對應客戶 id';
COMMENT ON COLUMN analysis_notes.title      IS '解析標題';
COMMENT ON COLUMN analysis_notes.content    IS '解析內容';
COMMENT ON COLUMN analysis_notes.sort_order IS '排列順序，數字越小越前面';
COMMENT ON COLUMN analysis_notes.created_at IS '建立時間';
COMMENT ON COLUMN analysis_notes.updated_at IS '最後更新時間';

-- 6. 諮詢記錄
CREATE TABLE IF NOT EXISTS consultation_logs (
    id                SERIAL PRIMARY KEY,
    client_id         INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    consultation_date TIMESTAMP,
    notes             TEXT,
    created_at        TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE  consultation_logs                   IS '與客戶的諮詢記錄';
COMMENT ON COLUMN consultation_logs.id                IS '流水號';
COMMENT ON COLUMN consultation_logs.client_id         IS '對應客戶 id';
COMMENT ON COLUMN consultation_logs.consultation_date IS '諮詢日期時間';
COMMENT ON COLUMN consultation_logs.notes             IS '諮詢內容記錄';
COMMENT ON COLUMN consultation_logs.created_at        IS '建立時間';

-- 7. 備份記錄
CREATE TABLE IF NOT EXISTS backup_records (
    id           SERIAL PRIMARY KEY,
    file_path    VARCHAR(500) NOT NULL,
    note         VARCHAR(100) DEFAULT '手動備份',
    created_at   TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE  backup_records           IS '資料庫備份紀錄';
COMMENT ON COLUMN backup_records.id        IS '流水號';
COMMENT ON COLUMN backup_records.file_path IS '備份檔案路徑';
COMMENT ON COLUMN backup_records.note      IS '備份說明，預設為手動備份';
COMMENT ON COLUMN backup_records.created_at IS '備份時間';

-- 8. 星盤設定
CREATE TABLE IF NOT EXISTS chart_preferences (
    id              SERIAL PRIMARY KEY,
    orb_conjunction DECIMAL(3,1) DEFAULT 8.0,
    orb_opposition  DECIMAL(3,1) DEFAULT 8.0,
    orb_trine       DECIMAL(3,1) DEFAULT 8.0,
    orb_square      DECIMAL(3,1) DEFAULT 6.0,
    orb_sextile     DECIMAL(3,1) DEFAULT 6.0
);

COMMENT ON TABLE  chart_preferences                IS '星盤全域設定，系統只有一筆（id=1）';
COMMENT ON COLUMN chart_preferences.id             IS '流水號，永遠為 1';
COMMENT ON COLUMN chart_preferences.orb_conjunction IS '合相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_opposition  IS '對分相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_trine       IS '三分相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_square      IS '四分相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_sextile     IS '六分相容許度（度）';

INSERT INTO chart_preferences (id) VALUES (1);

--20260611新增--
-- ============================================================
-- schema 新增：元素解析知識庫 (element_notes)
-- ============================================================
 
CREATE TABLE IF NOT EXISTS element_notes (
    id           SERIAL       PRIMARY KEY,
 
    -- 星座代碼（對應 code_references.code, category='sign'）
    -- 十二星座：a s d f g h j k l z x c
    sign_key     VARCHAR(10)  NOT NULL,
 
    -- 行星代碼（行星×星座解析時填入：Q/W/E/R/T/Y/U）
    -- NULL  → 純星座解析
    -- NOT NULL → 行星×星座解析
    planet_key   VARCHAR(10)  NULL,
 
    -- 宮位（1~12）
    -- NULL  → 星座特性頁籤（第一頁籤）
    -- 1~12  → 一宮~十二宮頁籤
    house_key    SMALLINT     NULL CHECK (house_key BETWEEN 1 AND 12),
 
    title        VARCHAR(200),
    content      TEXT,
 
    -- 標籤欄位（第一批先開欄位；第二批細作 UI）
    tag          VARCHAR(200),
 
    -- 同一組合下排列順序，數字越大越新（最新在最上）
    sort_order   INTEGER      NOT NULL DEFAULT 0,
 
    created_at   TIMESTAMP    DEFAULT NOW(),
    updated_at   TIMESTAMP    DEFAULT NOW()
);
 
-- ── 欄位說明 ────────────────────────────────────────────────
COMMENT ON TABLE  element_notes              IS '系統層級占星元素解析知識庫（星座、行星×星座）';
COMMENT ON COLUMN element_notes.id           IS '流水號';
COMMENT ON COLUMN element_notes.sign_key     IS '星座代碼，對應 code_references（category=sign）：a牡羊 s金牛 d雙子 f巨蟹 g獅子 h處女 j天秤 k天蠍 l射手 z摩羯 x水瓶 c雙魚';
COMMENT ON COLUMN element_notes.planet_key   IS '行星代碼：Q太陽 W月亮 E水星 R金星 T火星 Y木星 U土星；NULL=純星座解析';
COMMENT ON COLUMN element_notes.house_key    IS '宮位（1~12）；NULL=星座特性頁籤';
COMMENT ON COLUMN element_notes.title        IS '解析段落標題';
COMMENT ON COLUMN element_notes.content      IS '解析內容（純文字）';
COMMENT ON COLUMN element_notes.tag          IS '標籤（手動輸入，第二批細作）';
COMMENT ON COLUMN element_notes.sort_order   IS '排列順序，數字越大越新（最新在最上）';
COMMENT ON COLUMN element_notes.created_at   IS '建立時間';
COMMENT ON COLUMN element_notes.updated_at   IS '最後更新時間';
 
-- ── Index ──────────────────────────────────────────────────
-- 前端查詢必定帶 sign_key + planet_key + house_key
CREATE INDEX IF NOT EXISTS idx_element_notes_lookup
    ON element_notes (sign_key, planet_key, house_key);
 
-- ── 組合邏輯說明 ────────────────────────────────────────────
-- sign_key='a', planet_key=NULL, house_key=NULL  → 牡羊座  星座特性
-- sign_key='a', planet_key=NULL, house_key=1     → 牡羊座  一宮
-- sign_key='a', planet_key='Q',  house_key=NULL  → 太陽牡羊座  星座特性
-- sign_key='a', planet_key='Q',  house_key=1     → 太陽牡羊座  一宮
--
-- 後端 Service 層驗證規則：
--   1. sign_key 必須為合法星座代碼（a/s/d/f/g/h/j/k/l/z/x/c）
--   2. planet_key 若有值必須為合法行星代碼（Q/W/E/R/T/Y/U）
--   3. house_key 若有值必須為 1~12（已由 CHECK constraint 保證）
