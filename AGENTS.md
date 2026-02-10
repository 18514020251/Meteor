# Meteor 项目 - AGENTS.md

## 项目概述

Meteor 是一个基于 Spring Cloud 的微服务架构项目，采用模块化设计，包含多个业务模块和基础设施组件。该项目主要用于票务管理系统，支持电影票务、用户管理、订单处理等业务功能。

### 项目架构
- **微服务架构**: 基于 Spring Cloud 2023.0.1 和 Spring Boot 3.2.5
- **服务发现**: 使用 Nacos 作为服务注册与发现中心
- **API 网关**: 提供统一的 API 入口和认证授权
- **分布式配置**: 支持多环境配置管理

## 技术栈

### 核心框架
- **Spring Boot 3.2.5**: 基础框架
- **Spring Cloud 2023.0.1**: 微服务框架
- **Spring Cloud Alibaba 2023.0.1.2**: 阿里云微服务组件

### 数据持久化
- **MySQL 8.0**: 关系型数据库
- **MyBatis Plus 3.5.7**: ORM 框架
- **Redis**: 缓存和数据存储

### 消息队列
- **RabbitMQ**: 消息中间件
- **Spring AMQP**: 消息队列集成

### 认证授权
- **Sa-Token 1.37.0**: 轻量级权限认证框架

### 文件存储
- **MinIO**: 对象存储服务

### API 文档
- **SpringDoc OpenAPI 2.2.0**: API 文档生成

### 开发工具
- **Lombok 1.18.32**: 代码简化
- **Jacoco**: 代码覆盖率测试

## 模块结构

### 业务模块

| 模块名称 | 端口 | 功能描述 |
|---------|------|----------|
| meteor-user | 8081 | 用户管理模块，包含用户注册、登录、个人信息管理等 |
| meteor-gateway | 8080 | API 网关，统一入口、路由转发、认证授权 |
| meteor-order | 8087 | 订单管理模块，处理业务订单相关逻辑 |
| meteor-ticketing | 8085 | 票务管理模块，电影票务相关业务 |
| meteor-movie | 8086 | 电影信息管理模块 |
| meteor-merchant | - | 商家管理模块 |
| meteor-message | - | 消息服务模块 |
| meteor-admin | - | 后台管理模块 |

### 基础设施模块

| 模块名称 | 功能描述 |
|---------|----------|
| meteor-common | 通用工具类和公共组件 |
| meteor-common-starter | 通用 starter 配置 |
| meteor-redis-starter | Redis 配置 starter |
| meteor-satoken-starter | Sa-Token 认证 starter |
| meteor-minio-starter | MinIO 文件存储 starter |
| meteor-mp-starter | MyBatis Plus 配置 starter |
| meteor-mq-starter | 消息队列 starter |
| meteor-mq-contract | 消息契约定义 |
| meteor-mq-topology-starter | 消息拓扑配置 |
| meteor-id-starter | 分布式 ID 生成器 |
| meteor-api-contract | API 契约定义 |

## 环境依赖

### 基础设施服务
项目依赖以下外部服务，可通过 Docker Compose 启动：

```yaml
# 使用 docker-compose-dev.yml 启动依赖服务
services:
  mysql:3306     # 数据库
  redis:6379     # 缓存
  minio:9000     # 文件存储
  rabbitmq:5672  # 消息队列
  nacos:8848     # 服务注册中心
```

### 配置文件
- `application.yml`: 主配置文件
- `application-dev.yml`: 开发环境配置
- `application-local.yml`: 本地环境配置
- `application-prod.yml`: 生产环境配置

## 构建和运行

### 环境要求
- **Java 17**: 项目基于 Java 17 开发
- **Maven 3.6+**: 构建工具
- **Docker**: 容器化部署（可选）

### 构建命令

```bash
# 清理并构建整个项目
./mvnw clean compile

# 跳过测试构建
./mvnw clean compile -DskipTests

# 打包所有模块
./mvnw clean package

# 安装到本地仓库
./mvnw clean install
```

### 运行服务

#### 1. 启动基础设施服务
```bash
# 使用 Docker Compose 启动依赖服务
docker-compose -f docker-compose-dev.yml up -d
```

#### 2. 启动各个服务模块
```bash
# 启动网关服务
./mvnw spring-boot:run -pl meteor-gateway

# 启动用户服务
./mvnw spring-boot:run -pl meteor-user

# 启动订单服务
./mvnw spring-boot:run -pl meteor-order

# 启动票务服务
./mvnw spring-boot:run -pl meteor-ticketing

# 启动电影服务
./mvnw spring-boot:run -pl meteor-movie
```

#### 3. 服务访问地址

| 服务 | 访问地址 | 描述 |
|------|----------|------|
| 网关 | http://localhost:8080 | API 统一入口 |
| 用户服务 | http://localhost:8081 | 用户管理接口 |
| 订单服务 | http://localhost:8087 | 订单管理接口 |
| 票务服务 | http://localhost:8085 | 票务管理接口 |
| 电影服务 | http://localhost:8086 | 电影管理接口 |
| Nacos | http://localhost:8848 | 服务注册中心 |
| MinIO | http://localhost:9001 | 文件存储管理界面 |
| RabbitMQ | http://localhost:15672 | 消息队列管理界面 |

### 测试命令

```bash
# 运行所有测试
./mvnw test

# 运行指定模块测试
./mvnw test -pl meteor-user

# 生成测试覆盖率报告
./mvnw jacoco:report
```

## 开发规范

### 代码风格
- 使用 Lombok 简化代码
- 遵循 MyBatis Plus 最佳实践
- 统一使用 Sa-Token 进行认证授权

### 数据库规范
- 使用逻辑删除（deleted 字段）
- 表名和字段名使用下划线命名法
- 统一使用分布式 ID 生成器

### API 规范
- RESTful API 设计风格
- 统一异常处理机制
- 使用 OpenAPI 3.0 生成文档

### 配置管理
- 多环境配置文件分离
- 敏感信息使用环境变量
- 统一配置中心管理

## 部署说明

### 开发环境部署
1. 启动基础设施服务（Docker Compose）
2. 配置环境变量（数据库连接、Redis、消息队列等）
3. 启动各个微服务模块

### 生产环境部署
1. 配置生产环境参数
2. 使用 Docker 容器化部署
3. 配置负载均衡和监控

## 故障排除

### 常见问题
1. **端口冲突**: 检查各服务端口配置
2. **数据库连接失败**: 验证 MySQL 服务状态和连接参数
3. **服务注册失败**: 检查 Nacos 服务是否正常启动
4. **消息队列异常**: 验证 RabbitMQ 连接配置

### 日志查看
```bash
# 查看服务日志
tail -f logs/服务名.log

# 查看 Docker 容器日志
docker logs meteor-mysql
docker logs meteor-redis
docker logs meteor-nacos
```

## 扩展开发

### 添加新模块
1. 在根 pom.xml 中添加模块依赖
2. 创建新的模块目录和 pom.xml
3. 配置 application.yml 文件
4. 实现业务逻辑代码

### 自定义 Starter
参考现有的 starter 模块，创建可复用的配置组件。

---

*最后更新: 2026-02-10*