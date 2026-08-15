Date: 2026-08-15

100 VU
QPS: 1083.37
avg: 91.93 ms
p95: 109.46 ms
p99: 155.16 ms
success: 100%

150 VU
QPS: 1376.22
avg: 108.67 ms
p95: 139.05 ms
p99: 205.79 ms
success: 100%

200 VU
QPS: ~1388
avg: ~143.54 ms
p95: ~174.47 ms
p99: ~259.34 ms

观察:
150 → 200 VU 吞吐基本平台化，延迟明显上升；
当前本机 full-success 路径 knee point 约在 150 VU 左右。