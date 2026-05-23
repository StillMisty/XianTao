INSERT
    INTO
        xt_event_type(
            activity_type,
            code,
            name,
            description
        )
    VALUES(
        'TRAINING',
        'training_rare_herb_found',
        '发现珍稀药草',
        '在历练时偶然发现了一株{{herb}}，采集到 ×{{count}}，运气不错！'
    ),
    (
        'TRAINING',
        'training_ancient_ruins',
        '发现远古遗迹',
        '在历练地发现了一个此前不为人知的远古遗迹入口。'
    ),
    (
        'TRAINING',
        'training_meditation_epiphany',
        '静坐顿悟',
        '在历练中突有所感，就地打坐悟出些许天道碎片，获得 {{exp}} 修为。'
    ),
    (
        'TRAINING',
        'training_rival_encounter',
        '遇到竞争对手',
        '另一个修士也在同一片区域历练——他好像不太友善。'
    ),
    (
        'TRAINING',
        'training_buried_treasure',
        '挖出宝贝',
        '挖开一块松土发现了不知谁埋的宝贝：{{item}} ×{{count}}。'
    ),
    (
        'TRAINING',
        'training_strange_stone',
        '奇石异象',
        '奇石在你靠近时发出微光，竟是{{item}} ×{{count}}。'
    ),
    (
        'TRAINING',
        'training_evil_presence',
        '邪气侵体',
        '一阵莫名阴寒袭来，邪气侵体，受到 {{damage}} 点伤害。'
    ),
    (
        'TRAINING',
        'training_spirit_guide',
        '灵体引路',
        '友善灵体引领你去了一处灵气浓郁之地，获得 +{{exp}} 修为。'
    ),
    (
        'TRAINING',
        'training_qi_storm',
        '灵气风暴',
        '灵气狂暴形成风暴！你在风暴眼稳守心神修炼，获得 +{{exp}} 修为。'
    ),
    (
        'TRAINING',
        'training_beast_den_found',
        '发现妖兽巢穴',
        '发现妖兽巢穴，翻找到妖兽藏匿的宝贝。'
    );
