-- =============================================
-- 占星顧問後台系統 — 初始化 Schema  (v18)
-- 修改說明：
--   v17 → v18
--   1. house_rulers 新增 house_sign VARCHAR(10) NULL
--      記錄該宮位的起始星座（整宮制直接填入，其他宮位制亦可使用）
-- =============================================

-- 代碼對照表
CREATE TABLE IF NOT EXISTS code_references (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,
    category    VARCHAR(20) NOT NULL CHECK (category IN ('planet', 'sign', 'aspect', 'topic')),
    zh_name     VARCHAR(50) NOT NULL
);

COMMENT ON TABLE  code_references          IS '代碼對照表，統一管理行星、星座、相位、主題的代碼與中文名稱';
COMMENT ON COLUMN code_references.id       IS '流水號';
COMMENT ON COLUMN code_references.code     IS '代碼（唯一值），例如 Q=太陽、a=牡羊座、q=合相、general=核心特質';
COMMENT ON COLUMN code_references.category IS '分類：planet=行星、sign=星座、aspect=相位、topic=主題分類';
COMMENT ON COLUMN code_references.zh_name  IS '中文名稱，例如 太陽、牡羊座、合相';

INSERT INTO code_references (code, category, zh_name) VALUES
('Q',         'planet',  '太陽'),
('W',         'planet',  '月亮'),
('E',         'planet',  '水星'),
('R',         'planet',  '金星'),
('T',         'planet',  '火星'),
('Y',         'planet',  '木星'),
('U',         'planet',  '土星'),
('I',         'planet',  '天王星'),
('O',         'planet',  '海王星'),
('P',         'planet',  '冥王星'),
('‹',         'planet',  '北交點'),
('Z',         'planet',  'ASC'),
('X',         'planet',  'MC'),
('a',         'sign',    '牡羊座'),
('s',         'sign',    '金牛座'),
('d',         'sign',    '雙子座'),
('f',         'sign',    '巨蟹座'),
('g',         'sign',    '獅子座'),
('h',         'sign',    '處女座'),
('j',         'sign',    '天秤座'),
('k',         'sign',    '天蠍座'),
('l',         'sign',    '射手座'),
('z',         'sign',    '摩羯座'),
('x',         'sign',    '水瓶座'),
('c',         'sign',    '雙魚座'),
('q',         'aspect',  '合相'),
('t',         'aspect',  '六分相'),
('r',         'aspect',  '四分相'),
('e',         'aspect',  '三分相'),
('w',         'aspect',  '對分相'),
('general',   'topic',   '核心特質；本質'),
('career',    'topic',   '事業；職場；天賦'),
('love',      'topic',   '感情；婚姻；人際'),
('wealth',    'topic',   '金錢；財運；自我價值'),
('challenge', 'topic',   '盲點；危機；課題')
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

-- ============================================================
-- 3. 宮位守護星 (house_rulers)
-- 版本：v18（新增 house_sign 欄位）
-- ============================================================
CREATE TABLE IF NOT EXISTS house_rulers (
    id             SERIAL PRIMARY KEY,
    client_id      INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    house_number   INTEGER NOT NULL CHECK (house_number BETWEEN 1 AND 12),
    house_sign     VARCHAR(10) NULL,
    ruling_planet  VARCHAR(10),
    flies_to_house INTEGER CHECK (flies_to_house BETWEEN 1 AND 12),
    flies_to_sign  VARCHAR(10)
);

COMMENT ON TABLE  house_rulers                IS '各宮位守護星及其飛入位置';
COMMENT ON COLUMN house_rulers.id             IS '流水號';
COMMENT ON COLUMN house_rulers.client_id      IS '對應客戶 id';
COMMENT ON COLUMN house_rulers.house_number   IS '宮位編號（1~12）';
COMMENT ON COLUMN house_rulers.house_sign     IS '該宮位的起始星座代碼，參考 code_references（category=sign）；整宮制由上升星座依序推算';
COMMENT ON COLUMN house_rulers.ruling_planet  IS '守護星代碼，參考 code_references（category=planet）';
COMMENT ON COLUMN house_rulers.flies_to_house IS '守護星飛入的宮位（1~12）';
COMMENT ON COLUMN house_rulers.flies_to_sign  IS '守護星飛入的星座代碼，參考 code_references（category=sign）';

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

-- ============================================================
-- 5. 我的解析 (analysis_notes)
-- ============================================================
CREATE TABLE IF NOT EXISTS analysis_notes (
    id          SERIAL PRIMARY KEY,
    client_id   INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    title       VARCHAR(200),
    content     TEXT,
    planet_key  VARCHAR(10) NULL,
    sign_key    VARCHAR(10) NULL,
    house_key   SMALLINT    NULL CHECK (house_key BETWEEN 1 AND 12),
    topic       VARCHAR(20) NULL
                CHECK (topic IN ('general', 'career', 'love', 'wealth', 'challenge')),
    sort_order  INTEGER DEFAULT 0,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE  analysis_notes             IS '針對客戶命盤的解析筆記';
COMMENT ON COLUMN analysis_notes.id          IS '流水號';
COMMENT ON COLUMN analysis_notes.client_id   IS '對應客戶 id';
COMMENT ON COLUMN analysis_notes.title       IS '解析標題';
COMMENT ON COLUMN analysis_notes.content     IS '解析內容';
COMMENT ON COLUMN analysis_notes.planet_key  IS '對應行星代碼（選填）';
COMMENT ON COLUMN analysis_notes.sign_key    IS '對應星座代碼（選填）';
COMMENT ON COLUMN analysis_notes.house_key   IS '對應宮位（選填）：1~12；NULL=未標記';
COMMENT ON COLUMN analysis_notes.topic       IS '主題分類（選填）：general=核心特質、career=事業、love=感情、wealth=財富、challenge=課題；NULL=未分類';
COMMENT ON COLUMN analysis_notes.sort_order  IS '排列順序，數字越小越前面';
COMMENT ON COLUMN analysis_notes.created_at  IS '建立時間';
COMMENT ON COLUMN analysis_notes.updated_at  IS '最後更新時間';

CREATE INDEX IF NOT EXISTS idx_analysis_notes_tags
    ON analysis_notes (client_id, planet_key, sign_key, house_key);

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

COMMENT ON TABLE  backup_records            IS '資料庫備份紀錄';
COMMENT ON COLUMN backup_records.id         IS '流水號';
COMMENT ON COLUMN backup_records.file_path  IS '備份檔案路徑';
COMMENT ON COLUMN backup_records.note       IS '備份說明，預設為手動備份';
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

COMMENT ON TABLE  chart_preferences                 IS '星盤全域設定，系統只有一筆（id=1）';
COMMENT ON COLUMN chart_preferences.id              IS '流水號，永遠為 1';
COMMENT ON COLUMN chart_preferences.orb_conjunction IS '合相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_opposition  IS '對分相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_trine       IS '三分相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_square      IS '四分相容許度（度）';
COMMENT ON COLUMN chart_preferences.orb_sextile     IS '六分相容許度（度）';

INSERT INTO chart_preferences (id) VALUES (1);

-- ============================================================
-- 9. 元素解析知識庫 (element_notes)
-- ============================================================
CREATE TABLE IF NOT EXISTS element_notes (
    id           SERIAL       PRIMARY KEY,
    sign_key     VARCHAR(10)  NOT NULL,
    planet_key   VARCHAR(10)  NULL,
    house_key    SMALLINT     NULL CHECK (house_key BETWEEN 1 AND 12),
    title        VARCHAR(200),
    content      TEXT,
    tag          VARCHAR(200),
    topic        VARCHAR(20)  NULL
                 CHECK (topic IN ('general', 'career', 'love', 'wealth', 'challenge')),
    sort_order   INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT NOW(),
    updated_at   TIMESTAMP    DEFAULT NOW()
);

COMMENT ON TABLE  element_notes              IS '系統層級占星元素解析知識庫（星座、行星×星座）';
COMMENT ON COLUMN element_notes.sign_key     IS '星座代碼：a牡羊 s金牛 d雙子 f巨蟹 g獅子 h處女 j天秤 k天蠍 l射手 z摩羯 x水瓶 c雙魚';
COMMENT ON COLUMN element_notes.planet_key   IS '行星代碼：Q太陽 W月亮 E水星 R金星 T火星 Y木星 U土星；NULL=純星座解析';
COMMENT ON COLUMN element_notes.house_key    IS '宮位（1~12）；NULL=星座特性頁籤';
COMMENT ON COLUMN element_notes.topic        IS '主題分類：general=核心特質、career=事業、love=感情、wealth=財富、challenge=課題；NULL=未分類';
COMMENT ON COLUMN element_notes.sort_order   IS '排列順序，數字越大越新（最新在最上）';

CREATE INDEX IF NOT EXISTS idx_element_notes_lookup
    ON element_notes (sign_key, planet_key, house_key);

-- ============================================================
-- 10. 行運解析知識庫 (transit_notes)
-- ============================================================
CREATE TABLE IF NOT EXISTS transit_notes (
    id               SERIAL       PRIMARY KEY,
    transit_planet   VARCHAR(10)  NOT NULL,
    aspect_type      VARCHAR(10)  NULL,
    natal_planet     VARCHAR(10)  NULL,
    transit_house    SMALLINT     NULL CHECK (transit_house BETWEEN 1 AND 12),
    title            VARCHAR(200),
    content          TEXT,
    tag              VARCHAR(200),
    topic            VARCHAR(20)  NULL
                     CHECK (topic IN ('general', 'career', 'love', 'wealth', 'challenge')),
    sort_order       INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    DEFAULT NOW(),
    updated_at       TIMESTAMP    DEFAULT NOW()
);

COMMENT ON TABLE  transit_notes                  IS '系統層級行運解析知識庫（外行星×相位×本命星、外行星×過境宮位）';
COMMENT ON COLUMN transit_notes.transit_planet   IS '行運行星代碼：Y木星 U土星 I天王星 O海王星 P冥王星';
COMMENT ON COLUMN transit_notes.aspect_type      IS '相位代碼：q合相 w對分相 e三分相 r四分相 t六分相；NULL=過境宮位情境';
COMMENT ON COLUMN transit_notes.natal_planet     IS '本命星代碼：Q太陽 W月亮 E水星 R金星 T火星；NULL=過境宮位情境';
COMMENT ON COLUMN transit_notes.transit_house    IS '行運行星過境的宮位（1~12）；NULL=行運星×相位×本命星情境';
COMMENT ON COLUMN transit_notes.topic            IS '主題分類：general=核心特質、career=事業、love=感情、wealth=財富、challenge=課題；NULL=未分類';
COMMENT ON COLUMN transit_notes.sort_order       IS '排列順序，數字越大越新（最新在最上）';

CREATE INDEX IF NOT EXISTS idx_transit_notes_lookup
    ON transit_notes (transit_planet, aspect_type, natal_planet, transit_house);

-- =============================================
-- !! 以下為 Seed 測試資料（我的命盤）
-- 冪等：若 Joan 已存在則跳過
-- =============================================
DO $$
DECLARE
    v_id INTEGER;
BEGIN
    IF EXISTS (SELECT 1 FROM clients WHERE name = 'Joan') THEN
        RAISE NOTICE 'Joan 已存在，跳過 seed。';
        RETURN;
    END IF;

    -- 1. clients
    INSERT INTO clients (
        name, birth_place,
        asc_sign, asc_degree_num, asc_minute_num,
        mc_sign,  mc_degree_num,  mc_minute_num,
        created_at, updated_at
    ) VALUES (
        'Joan', '台灣',
        'j', 21, 58,
        'f', 22, 32,
        NOW(), NOW()
    ) RETURNING id INTO v_id;

    -- 2. planet_positions
    INSERT INTO planet_positions (client_id, planet, sign, degree_num, minute_num, house, is_retrograde, notes, is_lord) VALUES
        (v_id, 'Q',  'g', 17, 29, 10, FALSE, '得時，分數8',                    FALSE),
        (v_id, 'W',  's', 11, 25,  7, FALSE, '慢，分數8',                      FALSE),
        (v_id, 'E',  'f', 29, 47, 10, FALSE, '岐度29°，東出，分數12',           FALSE),
        (v_id, 'R',  'f',  9, 23,  9, FALSE, '東出，分數6，命主星',             TRUE),
        (v_id, 'T',  'h', 28, 46, 12, FALSE, '西入，分數3',                    FALSE),
        (v_id, 'Y',  'j', 11, 12, 12, FALSE, '得時，西入，分數3',               FALSE),
        (v_id, 'U',  'x', 27, 42,  5, TRUE,  '逆行，東出，分數10',              FALSE),
        (v_id, 'I',  'z', 19,  7,  3, TRUE,  '逆行',                           FALSE),
        (v_id, 'O',  'z', 19,  1,  3, TRUE,  '逆行',                           FALSE),
        (v_id, 'P',  'k', 22, 44,  1, FALSE, NULL,                             FALSE),
        (v_id, '‹',  'l',  8, 43,  2, TRUE,  '逆行',                           FALSE);

    -- 3. house_rulers（整宮制）
    INSERT INTO house_rulers (client_id, house_number, house_sign, ruling_planet, flies_to_house, flies_to_sign) VALUES
        (v_id,  1, 'j', 'R',  9, 'f'),
        (v_id,  2, 'k', 'T', 12, 'h'),
        (v_id,  3, 'l', 'Y', 12, 'j'),
        (v_id,  4, 'z', 'U',  5, 'x'),
        (v_id,  5, 'x', 'U',  5, 'x'),
        (v_id,  6, 'c', 'Y', 12, 'j'),
        (v_id,  7, 'a', 'T', 12, 'h'),
        (v_id,  8, 's', 'R',  9, 'f'),
        (v_id,  9, 'd', 'E', 10, 'f'),
        (v_id, 10, 'f', 'W',  7, 's'),
        (v_id, 11, 'g', 'Q', 10, 'g'),
        (v_id, 12, 'h', 'E', 10, 'f');

    -- 4. aspects
    INSERT INTO aspects (client_id, planet1, aspect_type, planet2, orb, notes) VALUES
        (v_id, 'W', 'e', 'R', 2.03, '月亮金星互容接納，本垣+三分'),
        (v_id, 'Q', 'e', 'U', NULL, '土星接納太陽，三分+界'),
        (v_id, 'Q', 'e', 'Y', NULL, '木星接納太陽，三分+十度'),
        (v_id, 'E', 'q', 'Y', NULL, '水星木星互容，水星岐度29°'),
        (v_id, 'R', 'q', 'Y', NULL, '金星木星互容接納，吉合'),
        (v_id, 'T', 'r', 'E', NULL, '水星接納火星（本垣+曜升+十度），四分'),
        (v_id, 'U', 'w', 'Q', 9.22, '土星五宮對分太陽十宮'),
        (v_id, 'Y', 'q', 'Z', NULL, '木星十二宮合上升'),
        (v_id, 'P', 'q', 'Z', NULL, '冥王星合上升，天蠍一宮');

    -- 5. analysis_notes
    INSERT INTO analysis_notes (client_id, title, content, planet_key, sign_key, house_key, topic, sort_order) VALUES
        (v_id, '上升天秤座 — 命主星金星九宮',
         '上升天秤座，命主星金星落巨蟹座九宮，曜升（旺）。靈魂驅力指向高等學習、跨文化探索與美學追求，透過九宮的哲學與文化場域展現自我價值。',
         'R', 'f', 9, 'general', 10),
        (v_id, '太陽獅子十宮 — 事業舞台',
         '太陽落獅子座十宮，得時（日間盤）。十宮事業與社會地位是主軸，獅子座的創意表達與被看見的需求透過職涯舞台展現，是命盤最強能量軸線之一。',
         'Q', 'g', 10, 'career', 9),
        (v_id, '水星巨蟹十宮岐度29° — 臨界轉化',
         '水星落巨蟹座29度（岐度），十宮，守護九宮與十二宮。岐度代表某主題走到最後的關鍵節點，同時具有事業（十宮）與情感導向（巨蟹）。水星與木星互容。',
         'E', 'f', 10, 'career', 8),
        (v_id, '月亮金牛七宮 — 互容接納',
         '月亮落金牛座七宮，與金星三分相且互容接納。感情與伴侶關係能量流暢，渴望安全感、穩定與感官連結，是命盤感情方面最和諧的配置。',
         'W', 's', 7, 'love', 7),
        (v_id, '土星水瓶五宮逆行 — 創作課題',
         '土星逆行水瓶座五宮，守護四宮與五宮。五宮創作與自我表達受到土星制約，需透過時間與紀律解鎖創作自由，逆行加深內化與自我審查的傾向。',
         'U', 'x', 5, 'challenge', 6),
        (v_id, '木星天秤十二宮 — 隱藏的祝福',
         '木星落天秤座十二宮，得時（西入）。十二宮的木星帶來隱密的幸運，在幕後、獨處或靈性修行中累積資源與智慧，守護三宮、六宮與九宮。',
         'Y', 'j', 12, 'wealth', 5),
        (v_id, '冥王星天蠍一宮 — 轉化力核心',
         '冥王星合上升落天蠍座一宮，能量極強。天生具備深度洞察力與強烈轉化能量，外在給人神秘感，一生底層主題圍繞權力、控制與深層轉化。',
         'P', 'k', 1, 'general', 4),
        (v_id, '火星處女十二宮 — 隱形工作力',
         '火星落處女座十二宮，守護二宮與七宮，西入（能量向內）。行動力隱藏，需要獨處才能充電，處女座火星極重細節，容易過度自我批評。',
         'T', 'h', 12, 'challenge', 3);

    RAISE NOTICE 'Joan seed 完成，client_id = %', v_id;
END $$;
