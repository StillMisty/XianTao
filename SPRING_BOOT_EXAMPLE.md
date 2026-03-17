# Spring Boot 使用示例

本项目展示了一个完整的 Spring Boot 3+ 使用示例，包含以下核心功能：

## 📁 项目结构

```
src/
├── main/java/top/stillmisty/xiantao/
│   ├── domain/user/
│   │   ├── controller/      # RESTful API 控制器
│   │   │   ├── UserController.java       # 用户管理 API
│   │   │   └── GameInfoController.java   # 游戏信息 API
│   │   ├── entity/          # 实体类
│   │   ├── service/         # 业务逻辑层
│   │   │   └── UserService.java
│   │   ├── vo/              # 视图对象
│   │   │   └── UserVO.java
│   │   └── enums/           # 枚举类
│   ├── config/              # 配置类
│   │   └── GameProperties.java
│   ├── handle/              # 统一响应处理
│   │   └── ResponseHandler.java
│   └── mapper/              # 数据访问层
└── test/                    # 测试代码
```

## 🎯 核心特性

### 1. RESTful API 设计

**用户管理 API** (`/api/users`):
- `GET /api/users/{id}` - 查询指定用户
- `GET /api/users?page=1&size=10&keyword=xxx` - 分页查询用户列表
- `POST /api/users` - 创建新用户
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户

**游戏信息 API** (`/api/game`):
- `GET /api/game/info` - 获取游戏配置信息
- `GET /api/game/status` - 获取游戏状态

### 2. 统一响应格式

所有 API 返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

错误响应：
```json
{
  "code": 404,
  "message": "用户不存在"
}
```

### 3. 类型安全配置

使用 `@ConfigurationProperties` 实现类型安全的配置管理：

```yaml
# application.yml
game:
  name: XianTao
  max-level: 100
  exp-multiplier: 1.5
  afk-enabled: true
  afk-reward-interval: 60
```

```java
// GameProperties.java
@ConfigurationProperties(prefix = "game")
public class GameProperties {
    private String name;
    private Integer maxLevel;
    private Double expMultiplier;
    // ...
}
```

### 4. 全局异常处理

使用 `@RestControllerAdvice` 进行统一的异常处理：

```java
@RestControllerAdvice
public class ResponseHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return ResponseHandler.error(e.getMessage());
    }
}
```

### 5. 依赖注入

使用构造函数注入（Spring Boot 3+ 推荐方式）：

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

### 6. 数据验证

使用 Jakarta Validation 进行参数验证：

```java
@PostMapping
public ResponseEntity<Map<String, Object>> createUser(
    @Valid @RequestBody User user) {
    // ...
}
```

### 7. 单元测试

使用 JUnit 5 和 Mockito 进行测试：

```java
@SpringBootTest
class UserServiceTest {
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        // 测试逻辑
    }
}
```

## 🚀 使用方法

### 启动应用

```bash
./gradlew bootRun
```

### API 测试示例

#### 1. 查询用户列表
```bash
curl http://localhost:8080/api/users?page=1&size=10
```

#### 2. 创建用户
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "张三",
    "level": 1
  }'
```

#### 3. 获取游戏配置
```bash
curl http://localhost:8080/api/game/info
```

## 📝 Spring Boot 3+ 新特性

1. **Jakarta EE 9+**: 从 `javax.*` 迁移到 `jakarta.*`
2. **Java 17+**: 需要 Java 17 或更高版本
3. **AOT 编译**: 支持 GraalVM 原生镜像
4. **改进的依赖注入**: 推荐使用构造函数注入
5. **Observability**: 增强的可观测性支持

## 🔧 技术栈

- **框架**: Spring Boot 3.x
- **ORM**: MyBatis-Flex
- **数据库**: PostgreSQL
- **测试**: JUnit 5 + Mockito
- **构建工具**: Gradle
- **Java 版本**: Java 25

## 📊 最佳实践

1. ✅ 使用 DTO/VO 分离实体和 API 响应
2. ✅ 统一的响应格式和异常处理
3. ✅ 类型安全的配置管理
4. ✅ 完善的单元测试
5. ✅ RESTful API 设计规范
6. ✅ 构造函数注入而非字段注入
7. ✅ 使用记录类（Record）简化代码（Java 16+）

## 🎓 学习资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Flex 文档](https://mybatis-flex.com/)
- [Spring Boot 3 迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
