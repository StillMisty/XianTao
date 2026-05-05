-- 为 xt_pill_resistance 添加代理主键，支持 MyBatis-Flex @Id 更新
ALTER TABLE xt_pill_resistance DROP CONSTRAINT xt_pill_resistance_pkey;
ALTER TABLE xt_pill_resistance ADD COLUMN id BIGSERIAL PRIMARY KEY;
ALTER TABLE xt_pill_resistance ADD CONSTRAINT uk_pill_resistance_user_template UNIQUE (user_id, template_id);
