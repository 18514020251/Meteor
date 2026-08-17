# Meteor M1B-05 压测执行说明

## 1. 本轮目的

这不是 M8 的最终性能验收，而是 **M1B-05 完成后的阶段性回归压测**。目标是和 M1B 前的历史基线使用同一条 `/ticketing/order/grab` 主链，量化新增的 `clientRequestId -> requestId -> Redis Reservation -> Semaphore` 对吞吐与尾延迟的影响，同时验证重复请求与舱壁补偿不会破坏库存。

历史可比数据（仓库已有记录）：

- 100 VU 全成功：约 977.60 QPS，P95 130.66 ms，P99 196.90 ms。
- 100 VU 另一轮：约 1083.37 QPS，P95 109.46 ms，P99 155.16 ms。
- 150 -> 200 VU 时吞吐已出现平台化，旧基线 knee point 约在 150 VU。

因此本轮先跑 `50 / 100 / 150 / 200 VU`。只有 200 VU 仍稳定，再扩到 300 / 500；1000 VU 留到独立压测机或 M8 正式基线，不建议在单机开发环境直接起步。

## 2. 压测前门禁

先执行：

```bash
mvn -pl meteor-ticketing -am test
```

至少确认以下测试通过：

```bash
mvn -pl meteor-ticketing -am \
  -Dtest=GrabOrderControllerTest,GrabOrderServiceImplTest,ReservationStateMachineTest \
  test
```

Redis 集成测试需要可用 Redis：

```bash
mvn -pl meteor-ticketing -am \
  -Dtest=GrabRequestIdResolverRedisIntegrationTest,TicketReservationRedisIntegrationTest \
  test
```

本压缩包所在执行容器没有 Maven，因此这些测试需要在开发机执行后再开始正式压测。

## 3. 使用独立压测数据域

M1B-05 时 **成功的 Reservation 仍会停留在 PRE_RESERVED**，因为 MySQL Reservation 持久化、DB 三态库存以及支付 Confirm/Release 都属于后续 M1B-06/M1C/M4。大流量压测会产生大量 Redis Reservation 和 Outbox/订单数据。

因此：

1. 不要使用日常开发数据做 full-success 大流量压测。
2. 最稳妥的做法是启动一套独立 Redis（例如另外的容器/端口），让 User、Ticketing、Order、Gateway 等本轮涉及服务统一指向这套 Redis；这样 Token 与业务 Key 仍处于同一数据域。
3. Ticketing / Order 数据库使用独立压测库或可随时重建的数据。
4. 每轮 full-success 运行前准备一个新的高库存 screening；不要在不同 VU 档位之间复用已经被扣减的场次。
5. 压完后直接清理/重建这套压测 Redis 与压测数据库，比逐 Key 清理更安全。

## 4. 为什么先直压 8085

Gateway 的 `/ticketing/**` 当前配置了 Redis `RequestRateLimiter`，按 token/IP 默认仅约 `1 req/s`、burst 2。直接通过 8080 压测会先测到网关限流，而不是 Ticketing 主链容量。

因此本轮服务基线默认：

```text
BASE_URL=http://127.0.0.1:8085
```

M8 再单独做 Gateway + RateLimiter 的端到端容量测试。

## 5. 压测时关闭高成本调试日志

`application-local.yml` 当前启用了 MyBatis stdout SQL 日志。正式 baseline 建议启动 Ticketing 时覆盖：

```text
LOG_LEVEL=warn
SA_TOKEN_IS_LOG=false
--mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.nologging.NoLoggingImpl
```

否则控制台 IO 会明显污染 QPS/P99。

## 6. 先跑幂等并发 Smoke

使用一个库存充足、刚预热的场次：

```bash
k6 run \
  -e TOKEN="<token>" \
  -e SCREENING_ID="<screeningId>" \
  -e VUS=50 \
  -e DURATION=5s \
  performance/k6/grab-m1b05-idempotency.js
```

预期：

- HTTP / Result envelope 基本 100% 正常；
- 同一个 `clientRequestId` 最多只有一个真正业务成功；
- 其余重放在当前 M1B-05 语义下主要表现为 `BUSY`；
- Redis stock 只减少一次；
- Outbox 只新增一份对应业务事实。

如果这里出现多次 SUCCESS / 多份 Outbox / 库存多扣，停止性能压测，先修正确性。

## 7. 跑 Fresh Request Baseline

单次：

```bash
k6 run \
  --summary-export performance/k6/results/m1b05-v100.json \
  -e TOKEN="<token>" \
  -e SCREENING_ID="<screeningId>" \
  -e VUS=100 \
  -e DURATION=30s \
  -e RUN_ID="m1b05-v100" \
  performance/k6/grab-m1b05-baseline.js
```

Windows PowerShell 矩阵：

```powershell
./performance/k6/run-m1b05-matrix.ps1 `
  -Token "<token>" `
  -ScreeningId "<screeningId>"
```

Linux/macOS：

```bash
TOKEN="<token>" SCREENING_ID="<screeningId>" \
  ./performance/k6/run-m1b05-matrix.sh
```

> 矩阵脚本不会自动重置库存。每个 VU 档位都应使用重新准备的高库存场次，或者逐档手动运行并替换 SCREENING_ID。

## 8. 每档必须记录的指标

k6：

- `http_reqs` / QPS
- `http_req_failed`
- `http_req_duration` P50/P95/P99
- `grab_success_duration` P50/P95/P99
- SUCCESS / SOLD_OUT / BUSY / NOT_READY / FAIL 数量

JVM：

```bash
jstat -gcutil <ticketing_pid> 1000
```

同时记录 Ticketing 进程 CPU、RSS/Heap、线程数。若可用 JFR，建议在 150/200 VU 档位各录一段 30~60 秒 JFR。

Redis：

```bash
redis-cli INFO memory
redis-cli INFO stats
redis-cli INFO commandstats
redis-cli --latency
```

MySQL：记录 Hikari 活跃/等待连接、TPS、慢 SQL；特别关注 `mq_outbox_event` INSERT。

RabbitMQ：记录 ready / unacked / publish rate / consumer rate，避免只看 HTTP QPS 而忽略 MQ 堆积。

## 9. 判读顺序

1. **先看正确性**：库存、Reservation、Outbox 是否符合预期。
2. 再看 100 VU 与历史 100 VU 的 SUCCESS QPS/P95/P99 差异。
3. 看 150 -> 200 是否仍是 knee point，还是因新增 Redis Lua 往更低并发移动。
4. 若 CPU 未满而吞吐平台化，优先看 DB pool、Redis RTT、Outbox insert、Semaphore 和 MQ。
5. 若 BUSY 在 800 并发前大量出现，检查 Semaphore permits 是否泄漏或 lease 回收是否异常。

## 10. 本轮结果表

| 场景 | VUs | SUCCESS QPS | 总 QPS | P50 | P95 | P99 | BUSY | 错误率 | CPU | Heap/GC | DB pool | Redis RTT | MQ depth |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|---|---|---|
| M1B05 fresh | 50 | | | | | | | | | | | | |
| M1B05 fresh | 100 | | | | | | | | | | | | |
| M1B05 fresh | 150 | | | | | | | | | | | | |
| M1B05 fresh | 200 | | | | | | | | | | | | |

完成这张表后再决定后续开发：如果 M1B-05 没有明显回退，继续 M1B-06；如果出现明显性能拐点前移，则先定位证据，再决定是否在当前阶段修复。
