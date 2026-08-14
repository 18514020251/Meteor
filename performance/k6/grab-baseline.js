import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';
import { Counter, Rate, Trend } from 'k6/metrics';

// ============================================================
// Meteor 抢票接口 k6 Baseline
//
// 目标：
// 1. 统计总体 HTTP 吞吐与延迟；
// 2. 区分 SUCCESS / SOLD_OUT / NOT_READY / BUSY / FAIL；
// 3. 为每一种业务结果单独统计 avg / med / P90 / P95 / P99 / max；
// 4. 每次迭代生成新的 clientRequestId，测试“新业务请求吞吐”；
// 5. 不设置业务性能阈值，先忠实记录 Baseline。
// ============================================================

// ===== 可通过 -e 覆盖的压测参数 =====
const BASE_URL =
    __ENV.BASE_URL || 'http://127.0.0.1:8085';

const SCREENING_ID =
    __ENV.SCREENING_ID || '8000000000000000046';

const VUS =
    Number(__ENV.VUS || 100);

const DURATION =
    __ENV.DURATION || '30s';

// 直接压 ticketing 服务仍需要 token。
// 如果传入的是原始 token，脚本自动补 Bearer 前缀。
const RAW_TOKEN =
    __ENV.TOKEN || '';

const AUTHORIZATION =
    RAW_TOKEN.startsWith('Bearer ')
        ? RAW_TOKEN
        : (RAW_TOKEN ? `Bearer ${RAW_TOKEN}` : '');


// ============================================================
// 业务结果计数
// ============================================================

const grabSuccess =
    new Counter('grab_success');

const grabSoldOut =
    new Counter('grab_sold_out');

const grabNotReady =
    new Counter('grab_not_ready');

const grabBusy =
    new Counter('grab_busy');

const grabFail =
    new Counter('grab_fail');

const grabUnknown =
    new Counter('grab_unknown');

const responseParseFail =
    new Counter('response_parse_fail');


// SUCCESS / 所有压测请求。
// HTTP 异常、响应解析失败、业务非 SUCCESS 都记为 false。
const grabSuccessRate =
    new Rate('grab_success_rate');


// ============================================================
// 按业务结果拆分响应耗时
// ============================================================
//
// 第二个参数 true 表示这是时间指标。
// k6 会按时间单位显示，并计算 avg / med / percentile。
// ============================================================

const grabSuccessDuration =
    new Trend(
        'grab_success_duration',
        true
    );

const grabSoldOutDuration =
    new Trend(
        'grab_sold_out_duration',
        true
    );

const grabNotReadyDuration =
    new Trend(
        'grab_not_ready_duration',
        true
    );

const grabBusyDuration =
    new Trend(
        'grab_busy_duration',
        true
    );

const grabFailDuration =
    new Trend(
        'grab_fail_duration',
        true
    );

const grabUnknownDuration =
    new Trend(
        'grab_unknown_duration',
        true
    );


// ============================================================
// k6 配置
// ============================================================

export const options = {

  scenarios: {

    grab_baseline: {

      executor: 'constant-vus',

      vus: VUS,

      duration: DURATION,

      gracefulStop: '5s',
    },
  },


  // Baseline 阶段只判断“压测本身是否健康”。
  //
  // 暂时不要写：
  // P95 < xx
  // QPS > xx
  //
  // 因为现在我们的目的就是测出真实 Baseline。
  thresholds: {

    http_req_failed: [
      'rate<0.01',
    ],

    checks: [
      'rate>0.99',
    ],
  },


  // 默认摘要没有我们想要的完整 P99，
  // 所以显式指定最终输出字段。
  summaryTrendStats: [

    'avg',

    'min',

    'med',

    'p(90)',

    'p(95)',

    'p(99)',

    'max',
  ],


  // 必须保留响应体，
  // 因为下面要解析业务结果。
  discardResponseBodies: false,
};


// ============================================================
// Setup
// ============================================================

export function setup() {

  if (!AUTHORIZATION) {

    throw new Error(
        '缺少 TOKEN。请使用 -e TOKEN=<登录返回的token>，' +
        '脚本会自动补 Bearer 前缀。'
    );
  }


  return {

    authorization:
    AUTHORIZATION,


    // 不传 RUN_ID 时自动生成。
    //
    // 这样不同压测批次不会重复使用
    // 同一批 clientRequestId。
    //
    // Windows CMD 下我们后面直接不传 RUN_ID 即可。
    runId:
        __ENV.RUN_ID || `${Date.now()}`,
  };
}


// ============================================================
// 压测主逻辑
// ============================================================

export default function (data) {

  // ----------------------------------------------------------
  // 1. 每次请求生成新的 clientRequestId
  // ----------------------------------------------------------
  //
  // 因此这里测试的是：
  //
  // 新请求
  // 新请求
  // 新请求
  //
  // 而不是：
  //
  // 同一个 clientRequestId 重试 100 次
  //
  // 后面 M1B-02 完成后，
  // 我们会另外写“幂等重试场景”。
  // ----------------------------------------------------------

  const clientRequestId =
      `k6-${data.runId}-` +
      `${exec.vu.idInTest}-` +
      `${exec.vu.iterationInScenario}`;


  // ----------------------------------------------------------
  // 2. 构造 JSON
  // ----------------------------------------------------------
  //
  // screeningId 是 8e18 量级，
  // 已经超过 JavaScript Number.MAX_SAFE_INTEGER。
  //
  // 所以不能：
  //
  // Number(SCREENING_ID)
  //
  // 否则 Long 可能发生精度丢失。
  //
  // 这里直接把数字文本拼进 JSON。
  // ----------------------------------------------------------

  const body =
      `{"screeningId":${SCREENING_ID},` +
      `"clientRequestId":"${clientRequestId}"}`;


  // ----------------------------------------------------------
  // 3. 发送请求
  // ----------------------------------------------------------

  const res =
      http.post(

          `${BASE_URL}/ticketing/order/grab`,

          body,

          {

            headers: {

              Authorization:
              data.authorization,

              'Content-Type':
                  'application/json',
            },


            tags: {

              name:
                  'POST /ticketing/order/grab',
            },
          }
      );


  // 整个 HTTP 请求耗时。
  //
  // 后面根据最终业务结果，
  // 把这个 duration 放到不同的 Trend 里。
  const duration =
      res.timings.duration;


  // ----------------------------------------------------------
  // 4. HTTP 层检查
  // ----------------------------------------------------------

  const httpOk =
      check(
          res,
          {

            'HTTP 200':
                (r) => r.status === 200,
          }
      );


  if (!httpOk) {

    grabSuccessRate.add(false);

    return;
  }


  // ----------------------------------------------------------
  // 5. JSON 解析
  // ----------------------------------------------------------

  let result;


  try {

    result =
        res.json();

  } catch (_) {

    responseParseFail.add(1);

    grabSuccessRate.add(false);

    return;
  }


  // ----------------------------------------------------------
  // 6. Meteor Result 外层检查
  // ----------------------------------------------------------

  const envelopeOk =
      check(
          result,
          {

            'Result.code == 200':
                (r) =>
                    r &&
                    r.code === 200,


            'Result.data exists':
                (r) =>
                    r &&
                    r.data != null,
          }
      );


  if (!envelopeOk) {

    grabUnknown.add(1);

    grabUnknownDuration.add(
        duration
    );

    grabSuccessRate.add(false);

    return;
  }


  // ----------------------------------------------------------
  // 7. 业务结果分类
  // ----------------------------------------------------------
  //
  // GrabOrderResultEnum
  //
  // 0 = SUCCESS
  // 1 = SOLD_OUT
  // 2 = NOT_READY
  // 3 = BUSY
  // 9 = FAIL
  //
  // Number(...) 是为了兼容 code 偶尔以字符串返回的情况。
  // ----------------------------------------------------------

  const businessCode =
      Number(
          result.data.code
      );


  switch (businessCode) {


      // ========================================================
      // SUCCESS
      // ========================================================

    case 0:

      grabSuccess.add(1);

      grabSuccessDuration.add(
          duration
      );

      grabSuccessRate.add(
          true
      );

      break;


      // ========================================================
      // SOLD OUT
      // ========================================================

    case 1:

      grabSoldOut.add(1);

      grabSoldOutDuration.add(
          duration
      );

      grabSuccessRate.add(
          false
      );

      break;


      // ========================================================
      // NOT READY
      // ========================================================

    case 2:

      grabNotReady.add(1);

      grabNotReadyDuration.add(
          duration
      );

      grabSuccessRate.add(
          false
      );

      break;


      // ========================================================
      // BUSY
      // ========================================================

    case 3:

      grabBusy.add(1);

      grabBusyDuration.add(
          duration
      );

      grabSuccessRate.add(
          false
      );

      break;


      // ========================================================
      // FAIL
      // ========================================================

    case 9:

      grabFail.add(1);

      grabFailDuration.add(
          duration
      );

      grabSuccessRate.add(
          false
      );

      break;


      // ========================================================
      // UNKNOWN
      // ========================================================

    default:

      grabUnknown.add(1);

      grabUnknownDuration.add(
          duration
      );

      grabSuccessRate.add(
          false
      );

      break;
  }
}