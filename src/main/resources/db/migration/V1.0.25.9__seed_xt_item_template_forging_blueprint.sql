-- 锻造图纸种子数据 (xt_item_template, type=FORGING_BLUEPRINT)
INSERT
    INTO
        xt_item_template(
            name,
            TYPE,
            properties,
            tags,
            description
        )
    VALUES(
        '砍柴刀图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '砍柴刀'
            ),
            'grade',
            1,
            'requirements',
            '{"RIGIDITY":{"min":5,"max":15},"TOUGHNESS":{"min":3,"max":10},"SPIRIT":{"min":0,"max":5}}' ::jsonb
        ),
        '["blueprint","forge","blade","entry"]' ::jsonb,
        '砍柴刀的锻造图纸，入门必修，村头铁匠都会。'
    ),
    (
        '青锋刀图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '青锋刀'
            ),
            'grade',
            2,
            'requirements',
            '{"RIGIDITY":{"min":12,"max":25},"TOUGHNESS":{"min":5,"max":15},"SPIRIT":{"min":2,"max":8}}' ::jsonb
        ),
        '["blueprint","forge","blade","uncommon"]' ::jsonb,
        '青锋刀的锻造图纸，以玄铁淬火为关键。'
    ),
    (
        '青冥剑图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '青冥剑'
            ),
            'grade',
            2,
            'requirements',
            '{"RIGIDITY":{"min":10,"max":22},"TOUGHNESS":{"min":5,"max":14},"SPIRIT":{"min":4,"max":10}}' ::jsonb
        ),
        '["blueprint","forge","sword","uncommon"]' ::jsonb,
        '青冥剑的锻造图纸，剑如青冥，锋从砺出。'
    ),
    (
        '纯钧剑图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '纯钧剑'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":18,"max":32},"TOUGHNESS":{"min":8,"max":20},"SPIRIT":{"min":8,"max":16}}' ::jsonb
        ),
        '["blueprint","forge","sword","rare"]' ::jsonb,
        '纯钧剑的锻造图纸，古法制剑，需天时地利。'
    ),
    (
        '开山斧图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '开山斧'
            ),
            'grade',
            2,
            'requirements',
            '{"RIGIDITY":{"min":14,"max":28},"TOUGHNESS":{"min":6,"max":14},"SPIRIT":{"min":1,"max":5}}' ::jsonb
        ),
        '["blueprint","forge","axe","uncommon"]' ::jsonb,
        '开山斧的锻造图纸，重器天成，一斧开山。'
    ),
    (
        '旋风斧图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '旋风斧'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":20,"max":38},"TOUGHNESS":{"min":10,"max":22},"SPIRIT":{"min":3,"max":10}}' ::jsonb
        ),
        '["blueprint","forge","axe","rare"]' ::jsonb,
        '旋风斧的锻造图纸，攻防一体，需要精纯的锻材。'
    ),
    (
        '亮银枪图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '亮银枪'
            ),
            'grade',
            2,
            'requirements',
            '{"RIGIDITY":{"min":10,"max":22},"TOUGHNESS":{"min":6,"max":14},"SPIRIT":{"min":2,"max":8}}' ::jsonb
        ),
        '["blueprint","forge","spear","uncommon"]' ::jsonb,
        '亮银枪的锻造图纸，枪出如龙，寒芒所至。'
    ),
    (
        '镇魔棍图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '镇魔棍'
            ),
            'grade',
            2,
            'requirements',
            '{"RIGIDITY":{"min":8,"max":18},"TOUGHNESS":{"min":6,"max":16},"SPIRIT":{"min":5,"max":12}}' ::jsonb
        ),
        '["blueprint","forge","staff","uncommon"]' ::jsonb,
        '镇魔棍的锻造图纸，刻阵加持，伏魔专用。'
    ),
    (
        '玄铁甲图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '玄铁甲'
            ),
            'grade',
            2,
            'requirements',
            '{"RIGIDITY":{"min":14,"max":28},"TOUGHNESS":{"min":8,"max":18},"SPIRIT":{"min":1,"max":5}}' ::jsonb
        ),
        '["blueprint","forge","armor","plate"]' ::jsonb,
        '玄铁甲的锻造图纸，硬甲铸成哐哐响，但绝对安全。'
    ),
    (
        '寒铁重甲图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '寒铁重甲'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":20,"max":38},"TOUGHNESS":{"min":12,"max":25},"SPIRIT":{"min":3,"max":10}}' ::jsonb
        ),
        '["blueprint","forge","armor","plate","rare"]' ::jsonb,
        '寒铁重甲的锻造图纸，表面凝霜，穿上霸气凛然。'
    ),
    (
        '灵蚕法袍图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '灵蚕法袍'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":3,"max":12},"TOUGHNESS":{"min":12,"max":28},"SPIRIT":{"min":6,"max":15}}' ::jsonb
        ),
        '["blueprint","forge","armor","cloth","rare"]' ::jsonb,
        '灵蚕法袍的锻造图纸，轻盈通透，仙气飘飘。'
    ),
    (
        '陨铁战甲图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '陨铁战甲'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":25,"max":45},"TOUGHNESS":{"min":15,"max":30},"SPIRIT":{"min":8,"max":18}}' ::jsonb
        ),
        '["blueprint","forge","armor","plate","epic"]' ::jsonb,
        '陨铁战甲的锻造图纸，天外陨铁铸就，星辰护体。'
    ),
    (
        '穿云弓图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '穿云弓'
            ),
            'grade',
            2,
            'requirements',
            '{"RIGIDITY":{"min":6,"max":16},"TOUGHNESS":{"min":8,"max":20},"SPIRIT":{"min":3,"max":8}}' ::jsonb
        ),
        '["blueprint","forge","bow","uncommon"]' ::jsonb,
        '穿云弓的锻造图纸，灵兽筋为弦，射程超远。'
    ),
    (
        '灵玉戒指图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '灵玉戒指'
            ),
            'grade',
            1,
            'requirements',
            '{"RIGIDITY":{"min":2,"max":10},"TOUGHNESS":{"min":3,"max":12},"SPIRIT":{"min":5,"max":12}}' ::jsonb
        ),
        '["blueprint","forge","accessory","ring"]' ::jsonb,
        '灵玉戒指的锻造图纸，嵌入灵石，灵气流转。'
    ),
    (
        '灵石吊坠图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '灵石吊坠'
            ),
            'grade',
            1,
            'requirements',
            '{"RIGIDITY":{"min":1,"max":8},"TOUGHNESS":{"min":3,"max":12},"SPIRIT":{"min":6,"max":14}}' ::jsonb
        ),
        '["blueprint","forge","accessory","necklace"]' ::jsonb,
        '灵石吊坠的锻造图纸，水滴灵玉，胸前暖阳。'
    ),
    (
        '玉镯图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '玉镯'
            ),
            'grade',
            1,
            'requirements',
            '{"RIGIDITY":{"min":2,"max":10},"TOUGHNESS":{"min":5,"max":15},"SPIRIT":{"min":4,"max":10}}' ::jsonb
        ),
        '["blueprint","forge","accessory","bracelet"]' ::jsonb,
        '玉镯的锻造图纸，翡翠手镯，温润养人。'
    ),
    (
        '龙鳞软甲图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '龙鳞软甲'
            ),
            'grade',
            5,
            'requirements',
            '{"RIGIDITY":{"min":30,"max":50},"TOUGHNESS":{"min":20,"max":40},"SPIRIT":{"min":12,"max":25}}' ::jsonb
        ),
        '["blueprint","forge","armor","leather","legendary"]' ::jsonb,
        '龙鳞软甲的锻造图纸，龙鳞为甲，轻而坚不可摧。'
    ),
    (
        '龙雀刀图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '龙雀刀'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":18,"max":32},"TOUGHNESS":{"min":10,"max":22},"SPIRIT":{"min":5,"max":12}}' ::jsonb
        ),
        '["blueprint","forge","blade","rare"]' ::jsonb,
        '龙雀刀的锻造图纸，刀身龙纹雀翎交映，刀锋所向无不俯首。'
    ),
    (
        '天刑刀图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '天刑刀'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":24,"max":42},"TOUGHNESS":{"min":14,"max":28},"SPIRIT":{"min":8,"max":18}}' ::jsonb
        ),
        '["blueprint","forge","blade","epic"]' ::jsonb,
        '天刑刀的锻造图纸，天刑刀出，代天行罚。需以先天庚金淬刃，否则难以驾驭。'
    ),
    (
        '七星剑图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '七星剑'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":18,"max":30},"TOUGHNESS":{"min":6,"max":16},"SPIRIT":{"min":10,"max":18}}' ::jsonb
        ),
        '["blueprint","forge","sword","rare"]' ::jsonb,
        '七星剑的锻造图纸，剑身分七段，每段对应北斗一星。月下锻剑，星光入刃。'
    ),
    (
        '诛仙剑图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '诛仙剑'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":26,"max":46},"TOUGHNESS":{"min":12,"max":24},"SPIRIT":{"min":12,"max":22}}' ::jsonb
        ),
        '["blueprint","forge","sword","epic"]' ::jsonb,
        '诛仙剑的锻造图纸，传说此剑铸成日天降血雨，非大毅力者不得持。'
    ),
    (
        '轩辕剑图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '轩辕剑'
            ),
            'grade',
            5,
            'requirements',
            '{"RIGIDITY":{"min":32,"max":55},"TOUGHNESS":{"min":18,"max":36},"SPIRIT":{"min":16,"max":28}}' ::jsonb
        ),
        '["blueprint","forge","sword","legendary"]' ::jsonb,
        '轩辕剑的锻造图纸，人皇之剑。一面刻日月星辰，一面刻山川草木。得此图者天下可期。'
    ),
    (
        '游龙枪图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '游龙枪'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":16,"max":30},"TOUGHNESS":{"min":8,"max":20},"SPIRIT":{"min":5,"max":12}}' ::jsonb
        ),
        '["blueprint","forge","spear","rare"]' ::jsonb,
        '游龙枪的锻造图纸，枪身柔韧如龙游水，可刺可扫变化万千。'
    ),
    (
        '霸王枪图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '霸王枪'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":26,"max":44},"TOUGHNESS":{"min":14,"max":28},"SPIRIT":{"min":6,"max":14}}' ::jsonb
        ),
        '["blueprint","forge","spear","epic"]' ::jsonb,
        '霸王枪的锻造图纸，枪重三百六十斤，非天生神力不可举。一枪之威可断江水。'
    ),
    (
        '流星弓图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '流星弓'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":12,"max":26},"TOUGHNESS":{"min":10,"max":24},"SPIRIT":{"min":6,"max":14}}' ::jsonb
        ),
        '["blueprint","forge","bow","rare"]' ::jsonb,
        '流星弓的锻造图纸，弓弦拉满如满月，箭出如流星追月。'
    ),
    (
        '帝江戟图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '帝江戟'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":24,"max":42},"TOUGHNESS":{"min":12,"max":26},"SPIRIT":{"min":8,"max":18}}' ::jsonb
        ),
        '["blueprint","forge","halberd","epic"]' ::jsonb,
        '帝江戟的锻造图纸，《山海经》帝江神形为基，无首而舞、万象归一。'
    ),
    (
        '雷霆战锤图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '雷霆战锤'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":28,"max":48},"TOUGHNESS":{"min":16,"max":30},"SPIRIT":{"min":6,"max":14}}' ::jsonb
        ),
        '["blueprint","forge","hammer","epic","thunder"]' ::jsonb,
        '雷霆战锤的锻造图纸，锤头刻雷纹，挥动时雷霆炸裂。锻造时需引天雷淬火。'
    ),
    (
        '东皇钟图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '东皇钟'
            ),
            'grade',
            5,
            'requirements',
            '{"RIGIDITY":{"min":20,"max":40},"TOUGHNESS":{"min":10,"max":25},"SPIRIT":{"min":22,"max":35}}' ::jsonb
        ),
        '["blueprint","forge","bell","legendary"]' ::jsonb,
        '东皇钟的锻造图纸，上古神器之图。钟声一响天地清，万法归宗莫能争。'
    ),
    (
        '云锦仙袍图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '云锦仙袍'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":8,"max":20},"TOUGHNESS":{"min":18,"max":36},"SPIRIT":{"min":12,"max":22}}' ::jsonb
        ),
        '["blueprint","forge","armor","cloth","epic"]' ::jsonb,
        '云锦仙袍的锻造图纸，采云为丝织就天衣。穿上如行云端，轻盈无尽。'
    ),
    (
        '玄武甲图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '玄武甲'
            ),
            'grade',
            5,
            'requirements',
            '{"RIGIDITY":{"min":34,"max":58},"TOUGHNESS":{"min":22,"max":42},"SPIRIT":{"min":10,"max":22}}' ::jsonb
        ),
        '["blueprint","forge","armor","plate","legendary"]' ::jsonb,
        '玄武甲的锻造图纸，玄武龟甲所化之铠，万法不侵。穿上即是一座城池。'
    ),
    (
        '乾坤戒图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '乾坤戒'
            ),
            'grade',
            3,
            'requirements',
            '{"RIGIDITY":{"min":6,"max":18},"TOUGHNESS":{"min":6,"max":18},"SPIRIT":{"min":12,"max":22}}' ::jsonb
        ),
        '["blueprint","forge","accessory","ring","rare"]' ::jsonb,
        '乾坤戒的锻造图纸，内藏乾坤的储物戒指。虚空石为核心，空间法则为辅。'
    ),
    (
        '盘龙金带图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '盘龙金带'
            ),
            'grade',
            4,
            'requirements',
            '{"RIGIDITY":{"min":10,"max":24},"TOUGHNESS":{"min":14,"max":30},"SPIRIT":{"min":10,"max":20}}' ::jsonb
        ),
        '["blueprint","forge","accessory","belt","epic"]' ::jsonb,
        '盘龙金带的锻造图纸，金丝缠绕盘龙纹饰，束腰如龙锁。'
    ),
    (
        '山河社稷图图',
        'FORGING_BLUEPRINT',
        jsonb_build_object(
            'equipment_template_id',
            (
                SELECT
                    id
                FROM
                    xt_equipment_template
                WHERE
                    name = '山河社稷图'
            ),
            'grade',
            5,
            'requirements',
            '{"RIGIDITY":{"min":12,"max":28},"TOUGHNESS":{"min":16,"max":34},"SPIRIT":{"min":20,"max":36}}' ::jsonb
        ),
        '["blueprint","forge","accessory","special","legendary"]' ::jsonb,
        '山河社稷图的锻造图纸，内藏一界之力。绘制此图的代价是百年寿元。'
    );
