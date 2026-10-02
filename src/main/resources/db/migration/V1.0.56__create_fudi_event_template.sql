-- 福地事件模板池（地灵对话懒生成，效果复用 SubEventEffect 参数格式）
CREATE TABLE fudi_event_template(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(64) NOT NULL,
    description      TEXT NOT NULL,
    effects          JSONB NOT NULL DEFAULT '[]'::jsonb,
    selection_weight INT NOT NULL DEFAULT 100,
    enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_fudi_event_template_weight CHECK(selection_weight > 0)
);

CREATE INDEX idx_fudi_event_template_enabled ON fudi_event_template(enabled);

COMMENT ON TABLE fudi_event_template IS '福地事件模板池（地灵对话 ≥4 小时懒生成）';
COMMENT ON COLUMN fudi_event_template.name IS '事件名称';
COMMENT ON COLUMN fudi_event_template.description IS '事件描述模板（{{key}} 占位符由机制效果结果填充）';
COMMENT ON COLUMN fudi_event_template.effects IS '机制效果 JSONB（SubEventEffect 同格式，空数组为纯叙事事件）';
COMMENT ON COLUMN fudi_event_template.selection_weight IS '选取权重，越大越容易被选中';
COMMENT ON COLUMN fudi_event_template.enabled IS '是否启用';

-- 上次福地事件时间（懒生成节流：≥4 小时才再生成，生成时以条件 UPDATE 原子占用保证只结算一次）
ALTER TABLE fudi ADD COLUMN last_event_time TIMESTAMP;

COMMENT ON COLUMN fudi.last_event_time IS '上次福地事件生成时间（地灵对话懒生成节流）';

-- 福地事件种子数据（12 条，其中 8 条带机制效果）
INSERT
    INTO
        fudi_event_template(
            name,
            description,
            effects,
            selection_weight
        )
    VALUES(
        '细雨绵绵',
        '细雨如丝，润物无声，福地灵气悄然充沛，吐纳间修为大有精进。（修为 +{{exp}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'ADD_EXP_PERCENT', 'percent', 5)
        ),
        100
    ),
    (
        '灵石矿脉微现',
        '福地地脉深处泛起微光，几枚灵石被泉水冲了出来，地灵替你一一收起。（灵石 +{{spiritStones}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'min', 2, 'max', 8)
        ),
        90
    ),
    (
        '地灵摘果',
        '地灵见你气色不佳，从树梢摘下一枚灵果递来。清甜入喉，气血为之一暖。（气血 +{{heal}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'HEAL_FLAT', 'amount', 30)
        ),
        100
    ),
    (
        '野兽叼来材料',
        '一只灵兽幼崽叼着东西溜进福地，放下战利品便蹦跳着跑远了。（获得 {{item}} ×{{count}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'ADD_RANDOM_ITEM', 'chance', 1.0, 'template_ids', jsonb_build_array(
                (SELECT id FROM item_template WHERE name = '兽骨'),
                (SELECT id FROM item_template WHERE name = '妖兽皮')
            ))
        ),
        80
    ),
    (
        '灵蝶飞舞',
        '一群发光的灵蝶在福地中翩翩起舞，翅尖抖落的灵光凝成灵石，落在你脚边。（灵石 +{{spiritStones}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'ADD_SPIRIT_STONES', 'amount', 3)
        ),
        100
    ),
    (
        '神秘访客',
        '一位行色匆匆的修士路过福地歇脚，与你谈玄论道半日，临走时留下谢礼。（获得 {{item}} ×{{count}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'ADD_RANDOM_ITEM', 'chance', 1.0, 'template_ids', jsonb_build_array(
                (SELECT id FROM item_template WHERE name = '玄铁矿石'),
                (SELECT id FROM item_template WHERE name = '灵木'),
                (SELECT id FROM item_template WHERE name = '朱砂')
            ))
        ),
        80
    ),
    (
        '灵兽诞生',
        '福地灵脉汇聚，一处暖光中孕育出新的生机，连吐纳都觉得畅快。（修为 +{{exp}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'ADD_EXP_PERCENT', 'percent', 8)
        ),
        80
    ),
    (
        '雷云过境',
        '一朵墨色劫云从福地上空缓缓掠过，雷气压得胸口发闷，灵气一时紊乱。（修为 {{exp}}）',
        jsonb_build_array(
            jsonb_build_object('type', 'ADD_EXP_PERCENT', 'percent', -5)
        ),
        60
    ),
    (
        '微风拂过',
        '微风拂过福地，草木沙沙作响，带来远方山野的气息。地灵坐在檐下，眯着眼听风。',
        '[]'::jsonb,
        100
    ),
    (
        '迷路的灵兽',
        '一只迷路的小灵兽闯进福地，怯生生地东张西望。地灵好言安抚，指了指出山的路。',
        '[]'::jsonb,
        90
    ),
    (
        '回忆旧主',
        '地灵望着院中老树出神，忽然说起从前那位主人——也是这般坐在树下，一坐就是半日。',
        '[]'::jsonb,
        80
    ),
    (
        '初遇回忆',
        '地灵忽然提起与主人初遇那日：山雾很重，你的脚步声却格外清晰。',
        '[]'::jsonb,
        80
    );
