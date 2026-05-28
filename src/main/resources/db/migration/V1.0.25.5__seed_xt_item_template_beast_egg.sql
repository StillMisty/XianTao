-- 兽卵种子数据 (xt_item_template, type=BEAST_EGG)
-- 通过 beast_template_id 引用 xt_beast_template 表中的配置
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
        '灵猫卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '灵猫')
        ),
        '["beast_egg","common","beast"]' ::jsonb,
        '温润的灵兽卵，表面隐约可见猫形纹路，散发着淡淡的灵光。'
    ),
    (
        '灵蚕卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '灵蚕')
        ),
        '["beast_egg","common","silk"]' ::jsonb,
        '晶莹剔透的灵蚕卵，内里可见丝丝银光流转。'
    ),
    (
        '玉兔卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '玉兔')
        ),
        '["beast_egg","common","beast","moon"]' ::jsonb,
        '月华凝结而成的灵卵，表面泛着柔和的银白光芒。'
    ),
    (
        '灵芝妖卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '灵芝妖')
        ),
        '["beast_egg","common","plant","heal"]' ::jsonb,
        '灵芝精华凝聚的妖卵，散发着浓郁的草木灵气。'
    ),
    (
        '铁羽鹰卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '铁羽鹰')
        ),
        '["beast_egg","uncommon","flying"]' ::jsonb,
        '坚硬如铁的灵卵，表面隐约可见鹰翼纹路。'
    ),
    (
        '雪狐卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '雪狐')
        ),
        '["beast_egg","uncommon","ice"]' ::jsonb,
        '通体雪白的灵卵，触之冰凉，散发着寒气。'
    ),
    (
        '火蟾卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '火蟾')
        ),
        '["beast_egg","uncommon","fire"]' ::jsonb,
        '赤红如火的灵卵，握在掌心能感受到灼热的温度。'
    ),
    (
        '碧鳞蛇卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '碧鳞蛇')
        ),
        '["beast_egg","uncommon","poison","beast"]' ::jsonb,
        '碧绿如玉的灵卵，表面隐现鳞片纹路，散发着淡淡腥气。'
    ),
    (
        '虎蛟卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '虎蛟')
        ),
        '["beast_egg","uncommon","beast","water"]' ::jsonb,
        '半透明的灵卵，内里似有水波流转，隐现虎纹蛟影。'
    ),
    (
        '比翼鸟卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '比翼鸟')
        ),
        '["beast_egg","uncommon","flying","support"]' ::jsonb,
        '成对出现的灵卵，两卵之间似有无形丝线相连。'
    ),
    (
        '青鸾卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '青鸾')
        ),
        '["beast_egg","rare","flying","phoenix"]' ::jsonb,
        '七彩霞光流转的灵卵，隐隐有凤鸣之声传出。'
    ),
    (
        '乘黄卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '乘黄')
        ),
        '["beast_egg","rare","beast","myth"]' ::jsonb,
        '金黄温润的灵卵，散发着祥和之气，令人身心舒畅。'
    ),
    (
        '金翼雕卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '金翼雕')
        ),
        '["beast_egg","rare","flying","predator"]' ::jsonb,
        '金光闪耀的灵卵，表面隐现金色羽翼纹路。'
    ),
    (
        '螭龙卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '螭龙')
        ),
        '["beast_egg","rare","dragon","sword"]' ::jsonb,
        '剑气纵横的灵卵，表面隐约可见龙纹，触之有锋锐之感。'
    ),
    (
        '天马卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '天马')
        ),
        '["beast_egg","rare","flying","speed"]' ::jsonb,
        '洁白如雪的灵卵，表面隐现双翼纹路，轻若无物。'
    ),
    (
        '黑水玄蛇卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '黑水玄蛇')
        ),
        '["beast_egg","rare","beast","ice","water"]' ::jsonb,
        '漆黑如墨的灵卵，散发着刺骨寒气，表面隐现蛇纹。'
    ),
    (
        '玄武龟卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '玄武龟')
        ),
        '["beast_egg","epic","beast","defense"]' ::jsonb,
        '厚重如山的灵卵，表面隐现龟蛇交缠之纹，坚不可摧。'
    ),
    (
        '九色鹿卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '九色鹿')
        ),
        '["beast_egg","epic","beast","auspicious","heal"]' ::jsonb,
        '九色霞光流转的灵卵，散发着祥瑞之气，令人心生欢喜。'
    ),
    (
        '白泽卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '白泽')
        ),
        '["beast_egg","epic","beast","auspicious","wis"]' ::jsonb,
        '通明如镜的灵卵，似能映照万物，蕴含无穷智慧。'
    ),
    (
        '夔牛卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '夔牛')
        ),
        '["beast_egg","legendary","beast","thunder"]' ::jsonb,
        '雷光缠绕的灵卵，不时传出隆隆雷声，令人敬畏。'
    ),
    (
        '穷奇卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '穷奇')
        ),
        '["beast_egg","legendary","beast","evil"]' ::jsonb,
        '煞气冲天的灵卵，表面隐现虎翼纹路，凶威凛凛。'
    ),
    (
        '朱雀卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '朱雀')
        ),
        '["beast_egg","legendary","phoenix","fire"]' ::jsonb,
        '烈焰环绕的灵卵，浴火不灭，散发着焚尽万物的气息。'
    ),
    (
        '青龙卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '青龙')
        ),
        '["beast_egg","legendary","dragon","wood"]' ::jsonb,
        '生机盎然的灵卵，表面隐现青龙纹路，草木逢春。'
    ),
    (
        '白虎卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '白虎')
        ),
        '["beast_egg","legendary","beast","metal"]' ::jsonb,
        '杀伐之气凝聚的灵卵，金光闪耀，虎啸隐隐。'
    ),
    (
        '麒麟卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '麒麟')
        ),
        '["beast_egg","legendary","auspicious","earth"]' ::jsonb,
        '瑞气千条的灵卵，祥云缭绕，万物安宁。'
    ),
    (
        '狻猊卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '狻猊')
        ),
        '["beast_egg","legendary","beast","fire"]' ::jsonb,
        '烟火缭绕的灵卵，狮形隐现，威压震慑四方。'
    ),
    (
        '饕餮卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '饕餮')
        ),
        '["beast_egg","legendary","beast","gluttony"]' ::jsonb,
        '贪婪之气凝聚的灵卵，似能吞噬一切，永无饱足。'
    ),
    (
        '混沌卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '混沌')
        ),
        '["beast_egg","legendary","beast","chaos"]' ::jsonb,
        '混沌未分的灵卵，浑然一体，蕴含天地初开之力。'
    ),
    (
        '梼杌卵',
        'BEAST_EGG',
        jsonb_build_object(
            'beast_template_id',
            (SELECT id FROM xt_beast_template WHERE name = '梼杌')
        ),
        '["beast_egg","legendary","beast","stubborn"]' ::jsonb,
        '顽固不化的灵卵，煞气凝重，战意滔天。'
    );
