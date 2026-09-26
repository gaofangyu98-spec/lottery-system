# lottery-system

基于 Spring Boot + RabbitMQ + Redis 的抽奖活动管理后台，支持活动/奖品/用户全生命周期管理，MQ 异步解耦 + 责任链状态机 + 消费失败手动补偿。

## 技术栈

| 组件 | 选型 |
|------|------|
| 框架 | Spring Boot 3.5.7 |
| JDK | 17 |
| 数据库 | MySQL + MyBatis |
| 缓存 | Redis (Lettuce) |
| 消息队列 | RabbitMQ (AMQP) |
| 鉴权 | JWT (jjwt 0.11.5) |
| 短信 | 阿里云 SMS |
| 邮件 | Spring Mail |
| 工具 | Lombok, Hutool, Jakarta Validation |
| 构建 | Maven |

## 项目结构

\\\
lottery-system/
├── pom.xml
├── src/main/java/com/example/lotterysystem/
│   ├── LotterySystemApplication.java        # 启动入口
│   ├── common/
│   │   ├── config/         # Spring 配置（拦截器、RabbitMQ、线程池、JWT）
│   │   ├── interceptor/     # 登录拦截器（JWT 校验）
│   │   ├── utils/           # 工具类（JWT、Redis、短信、邮件等）
│   │   ├── errorcode/       # 错误码体系
│   │   ├── exception/       # 统一异常
│   │   ├── filter/          # 日志过滤器
│   │   └── pojo/            # 通用返回体
│   ├── controller/
│   │   ├── param/           # 请求参数 DTO
│   │   ├── result/          # 响应结果 DTO
│   │   └── handler/         # 全局异常处理器
│   ├── dao/
│   │   ├── dataobject/      # 数据实体（含加密字段）
│   │   ├── mapper/          # MyBatis Mapper
│   │   └── handler/         # 自定义 TypeHandler
│   └── service/
│       ├── impl/            # 业务实现
│       ├── dto/             # 业务传输对象
│       ├── enums/           # 状态枚举
│       ├── mq/              # RabbitMQ 消费者（含死信补偿）
│       └── activitystatus/  # 责任链状态机
└── src/main/resources/
    ├── application.properties.example  # 配置模板
    ├── logback-spring.xml              # 日志配置
    └── static/                         # 前端管理页面
\\\

## 快速开始

### 前置依赖

- JDK 17+
- MySQL 8.0+
- Redis
- RabbitMQ
- Maven

### 配置

\\\ash
cp src/main/resources/application.properties.example src/main/resources/application.properties
\\\

编辑 \pplication.properties\，填入以下项：

| 配置 | 说明 |
|------|------|
| \spring.datasource.*\ | MySQL 连接信息 |
| \spring.data.redis.*\ | Redis 连接信息 |
| \spring.rabbitmq.*\ | RabbitMQ 连接信息 |
| \spring.mail.*\ | 邮箱账户和授权码 |
| \sms.access-key-id/secret\ | 阿里云短信 AccessKey |
| \jwt.secret\ | JWT 签名密钥（Base64 编码） |

### 构建运行

\\\ash
mvn clean package -Dmaven.test.skip=true
java -jar target/lottery-systemm-0.0.1-SNAPSHOT.jar
\\\

默认端口 \8082\，启动后访问 \http://localhost:8082/admin.html\。

## 核心功能

### 活动管理
- 活动 CRUD：名称、时间、状态、配额、参与人数上限
- 状态流转（待开始 → 进行中 → 已结束），责任链状态机
- 详情与分页列表

### 奖品管理
- 关联活动的奖品配置（等级、数量、概率）
- 奖品池管理与库存扣减

### 用户与鉴权
- 注册 / 密码登录 / 短信验证码登录
- JWT 令牌下发与拦截校验

### 异步与可靠性
- 抽奖请求 MQ 异步处理
- 死信队列（DLX）兜底失败消息，支持手动补偿
- Redis 防重复提交与缓存加速

## 安全说明

真实配置（\pplication.properties\ / \pplication-dev.properties\ / \pplication-test.properties\）已通过 \.gitignore\ 排除，不会提交到仓库。

1. 复制 \pplication.properties.example\ 为 \pplication.properties\
2. 填入真实数据库、Redis、MQ、短信 AccessKey、邮件授权码、JWT 密钥
3. JWT 密钥在本地配置中管理，源码无硬编码

## License

MIT
