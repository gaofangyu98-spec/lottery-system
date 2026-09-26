# lottery-system

基于 Spring Boot 3 + RabbitMQ + Redis 的抽奖活动管理后台，支持活动/奖品/用户全生命周期管理。采用 MQ 异步解耦抽奖流程、责任链状态机驱动活动状态流转、死信队列兜底失败消息并提供手动补偿。

## 核心亮点

| 亮点 | 说明 |
|---|---|
| MQ 异步抽奖 | 接口发消息毫秒返回，消费端完成校验/状态扭转/落库/通知，削峰填谷 |
| 责任链状态机 | 活动/奖品/人员三类状态由独立 Operator 按 sequence 顺序扭转，新增实体只加类不改主流程 |
| 手动补偿 + 死信兜底 | 消费失败时回滚三类状态、删中奖记录和缓存，MQ 重试 5 次后进死信队列 |
| Redis 多级缓存 | 活动详情缓存 3 天、中奖记录双维度缓存 2 天，写操作主动删缓存保证一致性 |
| 线程池异步通知 | 短信邮件通知扔给核心 10/最大 20 的线程池，不阻塞主链路 |
| 安全存储 | JWT 无状态鉴权、密码 SHA-256、手机号自定义 TypeHandler 加密落库 |

---

## 目录

- [架构总览](#架构总览)
- [项目结构](#项目结构)
- [技术栈](#技术栈)
- [核心功能](#核心功能)
- [活动状态机](#活动状态机)
- [异步抽奖流程](#异步抽奖流程)
- [异常补偿机制](#异常补偿机制)
- [Redis 缓存策略](#redis-缓存策略)
- [API 接口](#api-接口)
- [数据库模型](#数据库模型)
- [快速开始](#快速开始)
- [安全说明](#安全说明)
- [可改进点](#可改进点)

---

## 架构总览

```
客户端 (HTTP)
    │
    ▼
┌──────────────────────────────────────────────────────┐
│  Controller 层                                       │
│  Activity / Prize / User / DrawPrize 控制器          │
└─────────┬──────────┬──────────┬──────────────────────┘
          │          │          │
          ▼          ▼          ▼
┌──────────────────────────────────────────┐
│  Service 层                               │
│  ┌────────┐ ┌────────┐ ┌──────────────┐  │
│  │ 活动   │ │ 奖品   │ │ 抽奖         │  │
│  │ 管理   │ │ 管理   │ │ (异步)       │  │
│  └───┬────┘ └───┬────┘ └──────┬───────┘  │
│      │          │              │          │
│  ┌───▼──────────▼──────────────▼───────┐  │
│  │  活动状态机 (责任链模式)            │  │
│  │  ActivityStatusManager              │  │
│  │  → 待开始 / 进行中 / 已结束         │  │
│  └─────────────────────────────────────┘  │
└──────────────────────────────────────────┘
          │
          ▼
┌──────────────────────────────────────────┐
│  MQ 异步层                               │
│  ┌──────────────┐  ┌──────────────────┐  │
│  │ 抽奖队列      │  │ 死信队列 (DLX)   │  │
│  │ (异步削峰)   │  │ (失败补偿)       │  │
│  └──────┬───────┘  └────────┬─────────┘  │
│         │                   │            │
│         ▼                   ▼            │
│  ┌───────────────────────────────────┐   │
│  │  MqReceiver / DlxReceiver         │   │
│  └───────────────────────────────────┘   │
└──────────────────────────────────────────┘
          │
          ▼
┌──────────────────────────────────────────┐
│  数据层                                   │
│  MySQL (活动/奖品/用户/中奖记录)          │
│  Redis (缓存/防重复提交)                   │
└──────────────────────────────────────────┘
```

---

## 项目结构

```
lottery-system/
├── pom.xml
├── src/main/java/com/example/lotterysystem/
│   ├── LotterySystemApplication.java          # 启动入口
│   ├── common/
│   │   ├── config/          # App/ThreadPool/JWT/RabbitMQ 配置
│   │   ├── converter/       # 数据类型转换器
│   │   ├── interceptor/     # JWT 登录拦截器
│   │   ├── filter/          # 日志级别过滤器
│   │   ├── utils/           # JWT, Redis, SMS, Mail, Captcha, Regex
│   │   ├── errorcode/       # 统一错误码（全局/服务/控制器）
│   │   ├── exception/       # ControllerException, ServiceException
│   │   └── pojo/            # CommonResult 统一返回体
│   ├── controller/
│   │   ├── param/           # 请求参数 DTO
│   │   ├── result/          # 响应结果 DTO
│   │   └── handler/         # GlobalExceptionHandler
│   ├── dao/
│   │   ├── dataobject/      # 数据实体 (含加密字段 Encrypt)
│   │   ├── mapper/          # MyBatis Mapper
│   │   └── handler/         # EncryptTypeHandler 手机号加解密
│   └── service/
│       ├── impl/            # 业务实现
│       ├── dto/             # 业务传输对象
│       ├── enums/           # 状态/身份枚举
│       ├── mq/              # RabbitMQ 消费者 + DLX
│       └── activitystatus/  # 责任链状态机
│           ├── ActivityStatusManager.java
│           └── operater/    # Abstract/Activity/Prize/User 操作器
└── src/main/resources/
    ├── application.properties.example
    ├── logback-spring.xml
    └── static/              # admin.html 管理前端
```

---

## 技术栈

| 组件 | 选型 | 用途 |
|------|------|------|
| 框架 | Spring Boot 3.5.7 | 基础框架 |
| JDK | 17 | 运行环境 |
| 数据库 | MySQL + MyBatis | 数据持久化 |
| 缓存 | Redis (Lettuce) | 缓存加速 + 防重复提交 |
| 消息队列 | RabbitMQ | 异步抽奖 + 死信补偿 |
| 鉴权 | JWT (jjwt 0.11.5) | 登录令牌 |
| 短信 | 阿里云 SMS | 短信验证码 |
| 邮件 | Spring Mail | 通知 |
| 工具 | Lombok, Hutool, Jakarta Validation |
| 构建 | Maven |

---

## 核心功能

### 活动管理
- 活动 CRUD：名称、时间范围、状态、参与人数上限、配额
- 活动详情与分页列表
- 活动状态自动流转（待开始 → 进行中 → 已结束）

### 奖品管理
- 关联活动的奖品配置（等级、数量、中奖概率）
- 奖品池管理

### 用户与鉴权
- 注册 / 密码登录 / 短信验证码登录
- JWT 令牌下发与拦截校验
- 手机号加密落库，密码 SHA-256 哈希

### 抽奖流程
- 抽奖请求经 MQ 异步处理
- Redis 防重复提交（同用户对同活动限一次）
- 死信队列兜底失败消息，支持手动补偿

---

## 活动状态机

采用责任链模式实现活动状态流转，每个操作器（Operator）负责校验当前状态是否允许目标操作。

```
                    ┌──────────┐
                    │ 待开始    │
                    │ (PENDING)│
                    └────┬─────┘
                         │ 手动/定时开始
                         ▼
                    ┌──────────┐
                    │ 进行中    │
                    │ (ACTIVE) │
                    └────┬─────┘
                         │ 手动/定时结束
                         ▼
                    ┌──────────┐
                    │ 已结束    │
                    │ (END)    │
                    └──────────┘
```

### 状态枚举 (ActivityStatusEnum)
- PENDING — 待开始：可修改活动/奖品配置，不可抽奖
- ACTIVE — 进行中：可抽奖，不可修改配置
- END — 已结束：只读，展示中奖结果

### 责任链实现
- ActivityStatusManagerImpl — 状态管理器入口，根据操作类型分发
- AbstractActivityOperator — 活动操作器基类
- PrizeOperator — 奖品操作器：只能在 PENDING 状态修改奖品
- UserOperator — 用户操作器：只能在 ACTIVE 状态添加参与用户

为什么用责任链而不是 if-else：
1. 三类实体（活动/奖品/人员）各自有自己的扭转条件
2. 扭转有顺序依赖：先转人员和奖品，最后转活动（活动要等所有奖品抽完才结束）
3. 后续可能扩展新的实体类型，加一个 Operator 类就行，不改主流程

每个 Operator 三个方法：sequence() 定顺序、needConvert() 判断要不要转、convert() 执行扭转。Spring 按 Map 自动注入所有 Operator，Manager 按 sequence 排序执行。

---

## 异步抽奖流程

```
用户请求抽奖
    │
    ▼
┌──────────────────┐
│ Redis 防重复检查  │ ← SETNX(activityId:userId)
│ 是否已抽过？     │     成功则继续，失败返回"已参与"
└──────┬───────┬───┘
       │ 否    │ 是 → 返回"您已参与过该活动"
       ▼
┌──────────────────┐
│ MQ 发送抽奖消息   │ → RabbitMQ lottery.queue
│ (快速返回"处理中")│     消息体 = JSON 中奖名单 + UUID messageId
└──────┬───────────┘
       │ (异步消费)
       ▼
┌──────────────────────────────────────┐
│ MqReceiver 消费，分四步：              │
│ ① 校验：活动存在/奖品存在/状态合法     │
│ ② 状态扭转：责任链转人员+奖品+活动     │
│ ③ 落库：批量插中奖记录 + 写 Redis     │
│ ④ 通知：线程池发短信和邮件             │
└──────┬───────────────────────────────┘
       │ 失败
       ▼
┌──────────────────┐
│ 手动补偿回滚      │ → 回滚三类状态 + 删中奖记录 + 删缓存
│ 重新抛异常        │ → MQ 重试最多 5 次
│ 超限进 DLX       │ → DlxReceiver 接收
└──────────────────┘
```

### RabbitMQ 配置
- 直连交换机 (Direct Exchange)
- 自动确认模式 (acknowledge-mode=auto)
- 重试策略：最多 5 次
- 死信交换机：失败消息自动转发 DLX

### 消费端四步详解

1. **校验**（checkDrawPrizeParam）：活动是否存在、奖品是否存在、活动状态不是 COMPLETED、奖品状态不是 COMPLETED、传入中奖人数等于奖品设定数量
2. **状态扭转**（statusConvert）：在一个 @Transactional 事务里，通过责任链把中奖人员 INIT→COMPLETED、当前奖品 INIT→COMPLETED、活动 RUNNING→COMPLETED（仅当所有奖品都抽完）
3. **落库**（savewinnerRecords）：批量插 winning_record 表，写 Redis 中奖记录缓存
4. **通知**（syncExecute）：扔给线程池两个任务——发短信、发邮件，通知失败不回滚主数据

---

## 异常补偿机制

抽奖链路任何一步抛异常，catch 后做手动补偿：

1. 判断状态是否需要回滚：查当前奖品状态，如果已经是 COMPLETED 说明状态扭转过了，需要回滚；如果还是 INIT 说明状态没动过，只需要回滚中奖记录
2. 状态回滚：调 ActivityStatusManager 的 rollbackHandlerEvent，把活动改回 RUNNING、奖品改回 INIT、人员改回 INIT
3. 中奖记录回滚：删 winning_record 表数据，同时删 Redis 里奖品维度和活动维度的中奖记录缓存
4. 重新抛异常，触发 RabbitMQ 消费者重试（最多 5 次）
5. 超过重试次数进死信队列 DLX，由 DlxReceiver 处理

为什么判断回滚只看奖品状态：状态扭转在一个事务里，人员/奖品/活动要么全转要么全不转，看奖品状态就够了，不用三张表都查。

---

## Redis 缓存策略

两类缓存：

| Key | 内容 | 过期时间 |
|---|---|---|
| ACTIVITY_{activityId} | 完整活动信息 JSON（活动+奖品列表+人员列表） | 3 天 |
| WINNING_RECORDS_{activityId} | 活动维度全量中奖名单（活动完成后写） | 2 天 |
| WINNING_RECORDS_{activityId}_{prizeId} | 奖品维度中奖名单 | 2 天 |

缓存一致性：读请求先查 Redis，miss 了查库再回填；写操作（创建活动、扭转状态、删记录）主动删缓存或更新缓存，配合过期时间兜底，最终一致。

---

## API 接口

### 活动模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/activity/create | 创建活动 |
| POST | /api/activity/update | 更新活动 |
| GET | /api/activity/detail | 活动详情 |
| GET | /api/activity/list | 活动分页列表 |
| POST | /api/activity/update/status | 更新活动状态 |

### 奖品模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/prize/create | 创建奖品（全局） |
| POST | /api/prize/create/by-activity | 为活动创建奖品 |
| GET | /api/prize/list | 奖品列表 |

### 用户模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/register | 用户注册 |
| POST | /api/user/login | 密码登录 |
| POST | /api/user/sms-login | 短信验证码登录 |
| POST | /api/user/add/by-activity | 活动添加用户 |

### 抽奖模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/draw | 用户抽奖（MQ 异步） |
| GET | /api/draw/records | 中奖记录查询 |

---

## 数据库模型

### 主要表结构

| 表 | 说明 | 关键字段 |
|----|------|----------|
| activity | 活动表 | id, name, status, quota, start_time, end_time |
| prize | 奖品表 | id, name, level, total_count, probability, image_url |
| activity_prize | 活动-奖品关联 | activity_id, prize_id, remaining_count |
| user | 用户表 | id, name, password(SHA-256), phone(加密), email |
| activity_user | 活动参与用户 | activity_id, user_id, status |
| winning_record | 中奖记录 | id, activity_id, user_id, prize_id, prize_level |

设计要点：
- activity_prize 和 activity_user 是关联表，把"奖品/人员本身"和"它们在某个活动中的状态"拆开，同一个奖品可在多个活动复用
- winning_record 冗余存活动名/奖品名/中奖人信息，查记录不用 join，历史快照化

---

## 快速开始

### 前置依赖

- JDK 17+
- MySQL 8.0+
- Redis
- RabbitMQ
- Maven

### 配置

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

编辑 application.properties，填入以下项：

| 配置 | 说明 |
|------|------|
| spring.datasource.* | MySQL 连接 |
| spring.data.redis.* | Redis 连接 |
| spring.rabbitmq.* | RabbitMQ 连接 |
| spring.mail.* | 邮箱授权码 |
| sms.* | 阿里云短信 AccessKey |
| jwt.secret | JWT 签名密钥 (Base64) |

### 构建运行

```bash
mvn clean package -Dmaven.test.skip=true
java -jar target/lottery-systemm-0.0.1-SNAPSHOT.jar
```

默认端口 8082，启动后访问 http://localhost:8082/admin.html。

---

## 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| server.port | 8082 | 服务端口 |
| jwt.secret | (必填) | JWT 签名密钥，Base64 编码 |
| jwt.expiration | 3600000 | Token 过期时间(ms)，1 小时 |
| async.executor.thread.core_pool_size | 10 | 异步线程池核心数 |
| async.executor.thread.max_pool_size | 20 | 异步线程池最大数 |

---

## 安全说明

- 真实配置已通过 .gitignore 排除，不提交到仓库
- JWT 密钥在本地配置中管理，源码无硬编码
- 用户密码用 Hutool DigestUtil.sha256Hex 哈希存储，不存明文
- 手机号通过自定义 EncryptTypeHandler 在 MyBatis 读写时自动加解密，密文落库
- 所有用户输入在 Controller 层做参数校验（JSR-303）
- 短信验证码 5 分钟有效期，用完即焚
- 全局异常 GlobalExceptionHandler 统一返回 CommonResult

---

## 可改进点

1. 消息幂等：当前只生成 messageId 但没做唯一消费记录，重复消息可能重复处理（有状态校验兜底），改进是把 messageId 存 Redis 做消费记录
2. 死信队列：当前 DlxReceiver 直接重发会无限循环，生产上应该把死信消息落库，人工排查后再重发
3. 消费并发：当前单队列单消费者串行消费，要扩容可以多消费者 + 按活动 ID 分区
4. 通知重试：短信邮件失败没有重试机制，应该加通知状态表 + 定时补偿任务
5. 缓存击穿：热点活动缓存过期瞬间可能大量请求打 DB，可以加互斥锁或永不过期+异步刷新

---

## License

MIT
