-- 地灵情绪状态列（EmotionState 枚举 code，默认 NEUTRAL）
ALTER TABLE spirit ADD COLUMN emotion_state VARCHAR(32) NOT NULL DEFAULT 'NEUTRAL';

ALTER TABLE spirit
    ADD CONSTRAINT chk_spirit_emotion_state CHECK (
        emotion_state IN ('AFFECTIONATE', 'JOYFUL', 'CONTENT', 'NEUTRAL', 'DISTANT', 'WORRIED', 'EXCITED', 'ANGRY', 'EXHAUSTED')
    );

COMMENT ON COLUMN spirit.emotion_state IS '情绪状态（EmotionState 枚举 code：依恋/愉悦/满足/平和/疏离/忧虑/兴奋/愤怒/虚弱）';
