-- 福地事件模板种子数据
INSERT
    INTO
        fudi_event_template(
            name,
            description,
            effects,
            selection_weight
        )
    VALUES(
        '灵雨降世',
        '灵雨淅沥而下，草木欣然，福地灵气悄然增长。',
        '[{"type": "ADD_EXP_PERCENT", "percent": 5}]' ::jsonb,
        100
    ),
    (
        '山风穿堂',
        '山风穿堂而过，带来远方草木的清香，地灵惬意地眯起眼睛。',
        '[]' ::jsonb,
        100
    ),
    (
        '灵蝶飞舞',
        '一群发光的灵蝶翩然而至，在福地中起舞片刻后散作点点星光。',
        '[{"type": "ADD_SPIRIT_STONES", "amount": 3}]' ::jsonb,
        100
    ),
    (
        '神秘访客',
        '一位神秘的修士路过此地，留下一件小物件后飘然离去。',
        '[{"type": "ADD_RANDOM_ITEM", "poolId": "MATERIAL_COMMON"}]' ::jsonb,
        80
    ),
    (
        '灵草枯萎',
        '一株灵草无故枯黄，地灵围着它转了几圈，一脸困惑。',
        '[]' ::jsonb,
        90
    ),
    (
        '灵气恢复',
        '大地缓缓吐纳，福地灵气渐复，地灵舒服地叹了口气。',
        '[{"type": "HEAL_FLAT", "amount": 30}]' ::jsonb,
        100
    ),
    (
        '回忆旧主',
        '地灵忽然安静下来，目光悠远，似是想起了往事。',
        '[]' ::jsonb,
        80
    ),
    (
        '初遇回忆',
        '地灵偷偷看了你一眼，嘴角微微上扬，似乎在回忆初见时的情景。',
        '[]' ::jsonb,
        80
    ),
    (
        '物品失踪',
        '你翻遍了角落也没找到那件小东西——八成是被地灵藏起来了。',
        '[]' ::jsonb,
        70
    );
