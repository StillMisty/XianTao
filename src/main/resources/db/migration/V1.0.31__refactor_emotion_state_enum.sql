-- 重构地灵情绪枚举：新增多个状态灰度，重命名旧值
-- 1. 先删除旧约束，否则 UPDATE 会违反约束
ALTER TABLE xt_spirit DROP CONSTRAINT IF EXISTS chk_spirit_emotion_state;
ALTER TABLE xt_spirit_history DROP CONSTRAINT IF EXISTS chk_spirit_history_emotion_state;

-- 2. 将旧值映射到新值（存量数据迁移）
UPDATE xt_spirit SET emotion_state = 'joyful' WHERE emotion_state = 'happy';
UPDATE xt_spirit SET emotion_state = 'neutral' WHERE emotion_state = 'calm';
UPDATE xt_spirit SET emotion_state = 'worried' WHERE emotion_state = 'anxious';
UPDATE xt_spirit SET emotion_state = 'exhausted' WHERE emotion_state = 'fatigued';

UPDATE xt_spirit_history SET emotion_state = 'joyful' WHERE emotion_state = 'happy';
UPDATE xt_spirit_history SET emotion_state = 'neutral' WHERE emotion_state = 'calm';
UPDATE xt_spirit_history SET emotion_state = 'worried' WHERE emotion_state = 'anxious';
UPDATE xt_spirit_history SET emotion_state = 'exhausted' WHERE emotion_state = 'fatigued';

-- 3. 更新默认值
ALTER TABLE xt_spirit ALTER COLUMN emotion_state SET DEFAULT 'neutral';

-- 4. 添加新约束
ALTER TABLE xt_spirit ADD CONSTRAINT chk_spirit_emotion_state CHECK (
    emotion_state IN ('affectionate', 'joyful', 'content', 'neutral', 'distant', 'worried', 'excited', 'angry', 'exhausted')
);

ALTER TABLE xt_spirit_history ADD CONSTRAINT chk_spirit_history_emotion_state CHECK (
    emotion_state IS NULL OR emotion_state IN ('affectionate', 'joyful', 'content', 'neutral', 'distant', 'worried', 'excited', 'angry', 'exhausted')
);
