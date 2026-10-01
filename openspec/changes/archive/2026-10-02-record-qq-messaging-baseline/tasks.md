# Tasks

## 1. 平台约束查证

- [x] 1.1 查证腾讯官方文档（消息收发概述 / 发送群聊消息 / 错误与调试），记录：5 分钟窗口、每条消息最多 5 次、msg_seq 递增去重、Bot 60 qpm / 单群 20 qpm、按钮 label ≤10 字符、未报备 URL 禁止（见 design.md Context 与 qq-messaging 规格）

## 2. 实现核对与修复

- [x] 2.1 长回复分段与 msg_seq：`QQPlatformHandler.splitMessage`（1800B/4 段/换行优先/截断标注）+ `MessageSeqAllocator`；`MessageSplitTest` 4 条与 `QqMessageSenderTest` 通过
- [x] 2.2 按钮文字 10 字符：`QQPlatformHandler` 与 `NotificationAppender` 截断从 20 修为 10（含省略号）；`QQPlatformHandlerTest` 新增长度断言
- [x] 2.3 频控重试与按钮降级：`QqMessageSenderImpl.sendWithRetry` 限流退避（≤3 次）+ 按钮被 4xx 拒绝去按钮重发，单测覆盖
- [x] 2.4 空回复兜底与未投递事件补偿：`ReplyHelper.sanitize` 与 `NotificationAppender`，`ReplyHelperTest`/`NotificationAppenderTest` 覆盖

## 3. 规格与基线

- [x] 3.1 产出 `qq-messaging` 与 `game-features` 两份能力规格（本变更 `specs/` 目录）
- [x] 3.2 `AGENTS.md` 固化「不做主动推送」与「消息分段」两条硬约束

## 4. 验证

- [x] 4.1 `./gradlew build` 全绿（98 个测试，含 Spotless 与 NullAway）
- [x] 4.2 试玩复验：122 步（秘境列表可见、缺参提示、按钮、学习引导、文案中文化均符合预期）
- [x] 4.3 `openspec validate record-qq-messaging-baseline` 通过，随后归档为正式规格
