import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// Concurrent retries with ONE fixed clientRequestId.
// Expected on a fresh, in-stock screening:
// - at most one request can create the Reservation / Outbox business fact;
// - replays must not deduct stock or enter Semaphore/Outbox again;
// - current M1B-05 API represents Reservation replay as BUSY.

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:8085';
const SCREENING_ID = __ENV.SCREENING_ID || '';
const RAW_TOKEN = __ENV.TOKEN || '';
const VUS = Number(__ENV.VUS || 50);
const DURATION = __ENV.DURATION || '5s';
const RUN_ID = (__ENV.RUN_ID || `${Date.now()}`).replace(/[^a-zA-Z0-9_-]/g, '').slice(-16);
const CLIENT_REQUEST_ID = (__ENV.CLIENT_REQUEST_ID || `k6idem-${RUN_ID}`).slice(0, 64);
const AUTHORIZATION = RAW_TOKEN.startsWith('Bearer ') ? RAW_TOKEN : (RAW_TOKEN ? `Bearer ${RAW_TOKEN}` : '');

const success = new Counter('idem_success');
const busy = new Counter('idem_busy');
const soldOut = new Counter('idem_sold_out');
const notReady = new Counter('idem_not_ready');
const fail = new Counter('idem_fail');
const unknown = new Counter('idem_unknown');
const envelopeOkRate = new Rate('idem_envelope_ok_rate');
const durationByReplay = new Trend('idem_replay_duration', true);

export const options = {
  scenarios: {
    same_client_request_id: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
      gracefulStop: '3s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    idem_envelope_ok_rate: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export function setup() {
  if (!AUTHORIZATION) throw new Error('缺少 TOKEN。');
  if (!/^\d+$/.test(SCREENING_ID)) throw new Error('缺少或非法 SCREENING_ID。');
  return { authorization: AUTHORIZATION };
}

export default function (data) {
  const body = `{"screeningId":${SCREENING_ID},"clientRequestId":"${CLIENT_REQUEST_ID}"}`;
  const res = http.post(`${BASE_URL}/ticketing/order/grab`, body, {
    headers: {
      Authorization: data.authorization,
      'Content-Type': 'application/json',
    },
    tags: { name: 'POST /ticketing/order/grab [m1b05-idempotency]' },
    timeout: '10s',
  });

  if (!check(res, { 'HTTP 200': (r) => r.status === 200 })) {
    envelopeOkRate.add(false);
    return;
  }

  let result;
  try {
    result = res.json();
  } catch (_) {
    envelopeOkRate.add(false);
    unknown.add(1);
    return;
  }

  const envelopeOk = result && Number(result.code) === 200 && result.data != null;
  envelopeOkRate.add(envelopeOk);
  if (!envelopeOk) {
    unknown.add(1);
    return;
  }

  durationByReplay.add(res.timings.duration);
  switch (Number(result.data.code)) {
    case 0: success.add(1); break;
    case 1: soldOut.add(1); break;
    case 2: notReady.add(1); break;
    case 3: busy.add(1); break;
    case 9: fail.add(1); break;
    default: unknown.add(1); break;
  }
}
