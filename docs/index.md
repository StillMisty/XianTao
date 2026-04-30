# 仙道 (XianTao) — 项目文档

跨平台文字修仙 AFK MUD 游戏，核心特色为极低频率操作、懒加载离线计算、AI 驱动 NPC 沉浸感。

---

## 文档目录

| 文档 | 内容 |
|------|------|
| [更新路线图](./更新路线图.md) | Phase 1 ✅ / Phase 2–4 实施计划 |
| [系统架构与规范](./系统架构与规范.md) | 整体架构、DDD 分层、开发规范、工作流 |
| [用户](./用户.md) | 角色、认证、属性、等级、突破、护道 |
| [地图旅行历练](./地图旅行历练.md) | 地图节点、怪物、自动战斗懒结算、历练 |
| [物品装备](./物品装备.md) | 装备模板、法器类型、克制矩阵、稀有度加权随机 |
| [法决](./法决.md) | 法决模板、效果类型、法器绑定、槽位系统、自动轮播 |
| [悬赏系统](./悬赏.md) | 悬赏配置、奖励预确定、D20事件、LLM美化 |
| [福地](./福地.md) | 福地管理、灵气、地块、种植、献祭、天劫、蛰伏 |
| [地灵对话](./地灵对话.md) | LLM 集成、MBTI 人格、Function Calling、Prompt 设计 |
| [命令参考](./命令参考.md) | 完整玩家命令手册 |

---

## 技术栈

| 层 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 25 (Virtual Threads, Records, Sealed Classes) |
| 框架 | Spring Boot | 4.x |
| AI 集成 | Spring AI | 2.x |
| ORM | MyBatis-Flex | 1.x |
| 数据库 | PostgreSQL | 18 (JSONB 为主) |
| 机器人引擎 | SimBot | 4.x (OneBotV11 / QQ / Web) |
| 构建 | Gradle Kotlin DSL | |

## 数据库

### 已实现

| 表 | 迁移 | 用途 |
|------|------|------|
| `xt_user` | V1.0.1 | 玩家角色 |
| `xt_user_auth` | V1.0.2 | 跨平台身份绑定 |
| `xt_item_template` | V1.0.3 | 物品模板 |
| `xt_equipment` | V1.0.4 | 装备实例（含 weapon_type 列） |
| `xt_equipment_template` | V1.0.12 | 装备专用模板（weapon_type、category、攻速等） |
| `xt_inventory_item` | V1.0.5 | 堆叠物品 |
| `xt_map_node` | V1.0.6 | 地图节点 |
| `xt_dao_protection` | V1.0.7 | 护道关系 |
| `xt_fudi` | V1.0.8 | 福地 |
| `xt_spirit` | V1.0.9 | 灵兽 |
| `xt_bounty` | V1.0.10 | 悬赏模板 |
| `xt_user_bounty` | V1.0.11 | 悬赏进度 |

### 计划中 (Phase 2–3)

| 表 | 迁移 | 用途 |
|------|------|------|
| `xt_monster` | V1.0.13 | 怪物模板 |
| `xt_map_monster` | V1.0.14 | 地图-怪物关联 |
| `xt_spell` | V1.0.15 | 法决模板 |
| `xt_user_spell` | V1.0.16 | 用户已习得法决 |

## 初始地图

| 地图 | 类型 | 等级要求 | 相邻地图 |
|------|------|------|------|
| 黑金主城 | SAFE_TOWN | 0 | 幽暗沼泽(10min), 枯骨林(15min) |
| 幽暗沼泽 | TRAINING_ZONE | 5 | 黑金主城(10min), 迷雾洞窟(20min) |
| 枯骨林 | TRAINING_ZONE | 10 | 黑金主城(15min), 迷雾洞窟(25min) |
| 迷雾洞窟 | HIDDEN_ZONE | 20 | 幽暗沼泽(20min), 枯骨林(25min) |
