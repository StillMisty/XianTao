# 仙道 (XianTao) — 项目文档

跨平台文字修仙 AFK MUD 游戏，核心特色为极低频率操作、懒加载离线计算、AI 驱动 NPC 沉浸感。

---

## 文档目录

| 文档 | 内容 |
|------|------|
| [系统架构与规范](./系统架构与规范.md) | 整体架构、DDD 分层、开发规范、工作流 |
| [更新路线图](./更新路线图.md) | 装备/怪物/法决/战斗 四阶段实施计划 |
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

| 表 | 迁移 | 用途 |
|------|------|------|
| `xt_user` | V0.0.1 | 玩家角色 (22列, JSONB extra_data) |
| `xt_user_auth` | V0.0.1 | 跨平台身份绑定 |
| `xt_item_template` | V0.0.1 | 物品模板 |
| `xt_equipment` | V0.0.1 | 装备实例 (JSONB stat_bonus, affixes, GIN index) |
| `xt_equipment_template` | V2.0.0 | 装备专用模板 (weapon_type, category, 攻速等) |
| `xt_inventory_item` | V0.0.1 | 堆叠物品 (JSONB tags, GIN index) |
| `xt_map_node` | V0.0.2 | 地图节点 (JSONB neighbors, specialties, travel_events, GIN) |
| `xt_map_connection` | V0.0.2 | 地图连通关系 |
| `xt_monster` | V2.1.0 | 怪物模板 |
| `xt_map_monster` | V2.1.1 | 地图-怪物关联 |
| `xt_spell` | V2.2.0 | 法决模板 |
| `xt_user_spell` | V2.2.1 | 用户已习得法决 |
| `xt_dao_protection` | V0.0.3 | 护道关系 |
| `xt_fudi` | V0.0.4 | 福地 (JSONB grid_layout, spirit_config, GIN) |

## 初始地图

| 地图 | 类型 | 等级要求 | 相邻地图 |
|------|------|------|------|
| 黑金主城 | SAFE_TOWN | 0 | 幽暗沼泽(10min), 枯骨林(15min) |
| 幽暗沼泽 | TRAINING_ZONE | 5 | 黑金主城(10min), 迷雾洞窟(20min) |
| 枯骨林 | TRAINING_ZONE | 10 | 黑金主城(15min), 迷雾洞窟(25min) |
| 迷雾洞窟 | HIDDEN_ZONE | 20 | 幽暗沼泽(20min), 枯骨林(25min) |
