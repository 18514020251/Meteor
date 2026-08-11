# Meteor V2 系统不变量

## 1. 文档目的

本文档定义 Meteor V2 改造过程中必须始终遵守的核心业务不变量。

后续库存、请求幂等、RabbitMQ 消费、Transactional Outbox、订单支付与超时关闭等改造，都必须能够映射到本文中的至少一条不变量，并通过自动化测试、条件更新、CAS、Lua 原子操作或对账机制证明其正确性。

设计优先级：

> 正确性 > 可靠性 > 可维护性 > 性能

---

## 2. 数据权威边界

### 2.1 MySQL 是权威业务状态

MySQL 保存最终可审计的业务事实，包括：

* 库存状态
* Reservation
* Order
* Payment
* Outbox
* Consume Log

当 Redis、RabbitMQ 或应用进程出现故障时，最终业务状态必须能够根据数据库中的权威事实进行判断和恢复。

### 2.2 Redis 是高性能裁决与临时预留视图

Redis 用于：

* 抢票库存快速裁决
* Lua 原子库存操作
* 请求幂等身份解析
* Reservation 临时状态
* Semaphore 等高并发控制

Redis 状态丢失时，不能通过简单的 `+1 / -1` 猜测完整业务状态。

Redis Key 重建必须以可证明的权威业务事实为依据。

---

## 3. 库存不变量

### INV-STOCK-01｜库存守恒

任意合法状态下必须满足：

```text
totalTickets = availableTickets + reservedTickets + soldTickets
```

其中：

* `availableTickets`：仍可出售库存
* `reservedTickets`：已被 Reservation 占用但尚未完成最终交易的库存
* `soldTickets`：已经完成销售确认的库存

任何状态转换后：

```text
availableTickets >= 0
reservedTickets  >= 0
soldTickets      >= 0
```

### INV-STOCK-02｜Reserve

成功 Reserve 数量 `q` 后：

```text
available -= q
reserved  += q
```

且必须满足：

```text
available >= q
```

重复 Reserve 同一个 Reservation 不能重复扣库存。

### INV-STOCK-03｜Confirm

支付成功并确认库存时：

```text
reserved -= q
sold     += q
```

同一个 Reservation 只能成功 Confirm 一次。

### INV-STOCK-04｜Release

订单取消、超时或合法释放时：

```text
reserved  -= q
available += q
```

同一个 Reservation 只能成功 Release 一次。

重复 Release 不能重复增加库存。

### INV-STOCK-05｜补偿不能脱离业务身份

库存补偿最终必须绑定具体的 `reservationId`。

不能长期依赖无业务身份的：

```text
incrStockN(screeningId, 1)
```

作为最终补偿模型。

---

## 4. 请求幂等不变量

### INV-REQ-01｜clientRequestId 稳定

同一用户、同一场次、同一 `clientRequestId`，在业务参数指纹相同的情况下，只能映射到一个稳定的 `requestId`。

```text
(userId, screeningId, clientRequestId, fingerprint)
    ↓
stable requestId
```

### INV-REQ-02｜参数冲突必须被拒绝

同一个 `clientRequestId` 如果再次请求时业务参数指纹发生变化，不能当作同一个合法重试继续执行。

必须返回明确的冲突结果。

### INV-REQ-03｜一个 requestId 最多对应一个 Reservation

重复请求、网络重试或客户端重放不能产生第二份有效 Reservation，也不能重复扣库存。

---

## 5. Reservation 不变量

Reservation 允许的核心状态：

```text
PRE_RESERVED
    ├─ CONFIRMED
    ├─ RELEASED
    └─ COMPENSATED
```

### INV-RES-01｜终态不可反向转换

一旦 Reservation 进入：

```text
CONFIRMED
RELEASED
COMPENSATED
```

则不能重新回到 `PRE_RESERVED`。

### INV-RES-02｜Confirm 幂等

同一个 Reservation 的 Confirm 调用执行 N 次：

```text
业务效果最多成功一次
```

### INV-RES-03｜Release / Compensate 幂等

同一个 Reservation 的 Release 或 Compensate 调用执行 N 次：

```text
库存最多恢复一次
```

---

## 6. MQ 消费不变量

### INV-MQ-01｜At-Least-Once

Meteor 接受 RabbitMQ 的 At-Least-Once 投递语义。

同一消息允许被重复投递。

系统不追求消息传输层 exactly-once。

### INV-MQ-02｜重复消息无重复业务副作用

同一个 MQ message 重复投递 N 次：

```text
业务效果最多成功一次
```

消息层通过 `messageId / consume_log` 防重复。

业务层通过 `orderNo / reservationId / bizKey` 等唯一键进行第二层幂等保护。

### INV-MQ-03｜基础设施异常不能被误判为重复消费

只有明确的唯一键冲突，例如 `DuplicateKeyException`，才能作为“已经消费”的幂等结果处理。

数据库连接失败、SQL 异常、超时等基础设施问题必须继续抛出，使 RabbitMQ 可以重新投递或进入后续失败处理。

### INV-MQ-04｜Consume Log 与业务同事务

消费日志写入和业务操作必须属于同一个数据库事务。

如果业务执行失败：

```text
consume_log 必须一起回滚
```

否则下一次消息重投将无法重新执行正确业务逻辑。

---

## 7. Transactional Outbox 不变量

### INV-OUTBOX-01｜业务事实与待发送事件同事务

如果一个业务事务提交后必须产生 MQ 事件，则：

```text
业务数据
+
Outbox Event
```

必须在同一个本地数据库事务内提交。

不能依赖：

```text
DB commit
↓
直接 publish MQ
```

这种存在双写窗口的方式。

### INV-OUTBOX-02｜已提交事件不能静默消失

业务事务一旦成功提交，对应 Outbox 最终必须进入一个可审计状态，例如：

```text
SENT
DEAD
EXPIRED
```

不能永久无状态、静默丢失或无法追踪。

### INV-OUTBOX-03｜同一时刻只能有一个有效 Claim Owner

多实例 Dispatcher 并发扫描时，同一条 Outbox Event 只能由一个有效 Worker 持有。

Claim 必须由：

```text
lockedBy
lockedUntil
lockToken
```

等信息明确表达所有权。

### INV-OUTBOX-04｜旧 Worker 无权覆盖新 Owner

Worker A 的 lease 过期并被 Worker B 重新 Claim 后：

```text
Worker A 使用旧 lockToken 的状态更新必须失败
```

### INV-OUTBOX-05｜Confirm 成功但 DB Ack 失败允许重发

RabbitMQ Publisher Confirm 已成功，但 Outbox `markSent` 更新失败时，不追求 exactly-once。

事件可以在 lease / retry 后重新发送。

重复消息副作用由 Consumer 幂等机制兜底。

---

## 8. 订单状态不变量

订单核心状态竞争：

```text
WAIT_PAY
   ├─ PAID
   └─ CLOSED_TIMEOUT
```

### INV-ORDER-01｜支付与超时只能成功一条路径

订单从 `WAIT_PAY` 出发：

```text
WAIT_PAY → PAID
```

与：

```text
WAIT_PAY → CLOSED_TIMEOUT
```

只能有一个成功。

必须通过条件 UPDATE / CAS 等方式进行并发竞争，不能依赖“先查状态再更新”。

### INV-ORDER-02｜支付成功必须最终 Confirm 库存

订单成功进入：

```text
PAID
```

后，对应 Reservation 最终必须进入：

```text
CONFIRMED
```

对应库存最终：

```text
reserved → sold
```

### INV-ORDER-03｜超时关闭必须最终 Release 库存

订单成功进入：

```text
CLOSED_TIMEOUT
```

后，对应 Reservation 最终必须进入：

```text
RELEASED
```

对应库存最终：

```text
reserved → available
```

### INV-ORDER-04｜支付与超时事件不能双成功

如果支付路径已经成功：

```text
PAID
```

则超时关闭路径必须失败。

如果超时路径已经成功：

```text
CLOSED_TIMEOUT
```

则支付成功路径必须失败。

---

## 9. 可恢复性不变量

### INV-RECOVERY-01｜Redis Key 丢失不能导致超卖

Redis stock key 丢失时，不能直接无条件执行：

```text
Redis stock = DB availableTickets
```

必须先明确在途 Reservation 的归属。

必要时进入：

```text
REBUILDING
↓
暂停新 Reserve
↓
收敛在途 Reservation
↓
读取数据库权威状态
↓
重建 Redis
↓
恢复销售
```

### INV-RECOVERY-02｜未知事务结果不能盲目补偿

发生以下情况：

```text
数据库可能已提交
但 Java 调用返回异常
```

不能仅凭 `catch(Exception)` 直接恢复 Redis 库存。

必须先通过：

```text
reservationId
outbox bizKey
```

查询数据库权威状态，再决定是否执行补偿。

---

## 10. 测试映射原则

后续每条关键不变量至少必须对应一种验证方式：

* 单元测试
* Redis Lua 测试
* 数据库条件 SQL 测试
* MQ 重复投递测试
* Outbox 多实例测试
* 并发测试
* 故障注入测试
* 对账规则

测试优先验证：

```text
成功
重复
异常
并发
崩溃恢复
```

而不仅仅验证 Happy Path。

---

## 11. 当前 M0 阶段已建立的测试保护

`GrabOrderServiceImplTest` 当前覆盖：

```text
NOT_READY
SOLD_OUT
SUCCESS
Semaphore reject
Outbox insert failure
```

其中 M0-03 只负责建立当前核心分支的稳定回归入口。

库存补偿正确性将在 M0-05 中通过红灯测试单独证明，不能把当前错误行为固化为业务契约。
