import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';
import { Counter, Rate, Trend } from 'k6/metrics';

// M1B-05 fresh-request baseline.
// Each iteration uses a new clientRequestId, so this measures the normal
// requestId -> Reservation -> Semaphore -> Outbox path rather than retries.

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:8085';
const SCREENING_ID = __ENV.SCREENING_ID || '';
const RAW_TOKEN = __ENV.TOKEN || '';
const VUS = Number(__ENV.VUS || 100);
const DURATION = __ENV.DURATION || '30s';
const RUN_ID = (__ENV.RUN_ID || `${Date.now()}`).replace(/[^a-zA-Z0-9_-]/g, '').slice(-16);
const AUTHORIZATION = RAW_TOKEN.startsWith('Bearer ') ? RAW_TOKEN : (RAW_TOKEN ? `Bearer ${RAW_TOKEN}` : '');

const grabSuccess = new Counter('grab_success');
const grabSoldOut = new Counter('grab_sold_out');
const grabNotReady = new Counter('grab_not_ready');
const grabBusy = new Counter('grab_busy');
const grabFail = new Counter('grab_fail');
const grabUnknown = new Counter('grab_unknown');
const responseParseFail = new Counter('response_parse_fail');
const envelopeOkRate = new Rate('grab_envelope_ok_rate');

const grabSuccessDuration = new Trend('grab_success_duration', true);
const grabSoldOutDuration = new Trend('grab_sold_out_duration', true);
const grabNotReadyDuration = new Trend('grab_not_ready_duration', true);
const grabBusyDuration = new Trend('grab_busy_duration', true);
const grabFailDuration = new Trend('grab_fail_duration', true);
const grabUnknownDuration = new Trend('grab_unknown_duration', true);

export const options = {
  scenarios: {
    grab_m1b05_baseline: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
      gracefulStop: '5s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    grab_envelope_ok_rate: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
  discardResponseBodies: false,
};

export function setup() {
  if (!AUTHORIZATION) {
    throw new Error('缺少 TOKEN，请使用 -e TOKEN=<token>。');
  }
  if (!/^\d+$/.test(SCREENING_ID)) {
    throw new Error('缺少或非法 SCREENING_ID，请使用 -e SCREENING_ID=<long>。');
  }
  return { authorization: AUTHORIZATION };
}

function classify(result, duration) {
  const businessCode = Number(result.data.code);
  switch (businessCode) {
    case 0:
      grabSuccess.add(1);
      grabSuccessDuration.add(duration);
      break;
    case 1:
      grabSoldOut.add(1);
      grabSoldOutDuration.add(duration);
      break;
    case 2:
      grabNotReady.add(1);
      grabNotReadyDuration.add(duration);
      break;
    case 3:
      grabBusy.add(1);
      grabBusyDuration.add(duration);
      break;
    case 9:
      grabFail.add(1);
      grabFailDuration.add(duration);
      break;
    default:
      grabUnknown.add(1);
      grabUnknownDuration.add(duration);
      break;
  }
}

export default function (data) {
  // Keep clientRequestId <= 64 chars and preserve the changing suffix.
  const clientRequestId = `k6m1b05-${RUN_ID}-${exec.vu.idInTest}-${exec.vu.iterationInScenario}`;
  const body = `{"screeningId":${SCREENING_ID},"clientRequestId":"${clientRequestId}"}`;

  const res = http.post(`${BASE_URL}/ticketing/order/grab`, body, {
    headers: {
      Authorization: data.authorization,
      'Content-Type': 'application/json',
    },
    tags: { name: 'POST /ticketing/order/grab [m1b05-fresh]' },
    timeout: '10s',
  });

  const httpOk = check(res, { 'HTTP 200': (r) => r.status === 200 });
  if (!httpOk) {
    envelopeOkRate.add(false);
    return;
  }

  let result;
  try {
    result = res.json();
  } catch (_) {
    responseParseFail.add(1);
    envelopeOkRate.add(false);
    return;
  }

  const envelopeOk = check(result, {
    'Result.code == 200': (r) => r && Number(r.code) === 200,
    'Result.data exists': (r) => r && r.data != null,
  });
  envelopeOkRate.add(envelopeOk);

  if (!envelopeOk) {
    grabUnknown.add(1);
    grabUnknownDuration.add(res.timings.duration);
    return;
  }

  classify(result, res.timings.duration);
}
