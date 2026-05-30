# ADR-0002: 事件拦截器优先级重排 — @Filter 先于 @RequireAuth

## 状态

已接受

## 背景

事件拦截器执行顺序为 `@RequireAuth` (priority = -100) → `@RequireGm` (default 0) → `@Filter` (IN_LISTENER 模式，始终在所有拦截器之后执行)。这导致每一条 `MessageEvent` 都会触发 auth 查询（虽然同一事件跨 handler 复用），即使消息不匹配任何命令。

auth 层在 filter 之前执行，语义上也不合理——应先筛选再认证。

## 决策

1. 将所有 `@Filter` 切换为 `INTERCEPTOR` 模式（`FilterMode.INTERCEPTOR`），使其作为 `EventInterceptor` 注册到 handler 的局部拦截器链中
2. 调整优先级：`@Filter` (priority = 50) → `@RequireAuth` (priority = 100) → `@RequireGm` (priority = 200)
3. 保留 `@ContentTrim` (priority = 0) 在 filter 之前执行

## 理由

1. **性能**：非匹配消息在 filter 阶段即返回 `EventResult.invalid()`，auth 查询完全不执行。群聊场景下大部分消息不匹配，大幅减少 DB 开销
2. **语义正确**：筛选 → 认证 → 授权，每层逐级收窄
3. **框架支持**：SimBot 中 `@Filter(mode = INTERCEPTOR)` 和 `@Interceptor` 都创建局部 `EventInterceptor`，参与同一优先级体系

## 后果

### 正面
- 非匹配消息绕过 auth 层，减少 DB 查询
- 拦截器职责清晰，优先级可调

### 负面
- `@ContentTrim` (priority = 0) 必须在 `@Filter(INTERCEPTOR)` 之前执行，否则 filter 看到未 trim 的文本导致匹配失败。最终 `@Filter` 使用 priority = 50 解决
- 77 处 `@Filter` 注解需要批量更新模式名

### 注意事项
- `@Filter(mode = INTERCEPTOR)` 在匹配失败时返回 `EventResult.invalid()`，框架将其视为"此 handler 不适用"，继续尝试下一 handler
- `@Filter` 的 `value` 参数（含 `{{name}}` 模板语法）在 INTERCEPTOR 模式下正常工作，与 IN_LISTENER 模式一致
- `@Filter` 的 `priority` 默认值为 0，与 `@ContentTrim` 冲突，因此需要显式设为 50

## 相关决策

- `AuthInterceptorFactory`: 移除 `bindIfAbsent` 返回值检查（`retrieveFromEvent` 已处理并发）
- `GmInterceptorFactory`: 合并拒绝逻辑为单一出口；GM 查询简化为 `userRepository.findById()`
- `UserContext`: 使用 `WeakHashMap` 替代 `ConcurrentHashMap`，避免 `try/finally` 模式在协程调度下的竞态条件

## 日期

2026-05-31
