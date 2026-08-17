param(
    [Parameter(Mandatory=$true)]
    [string]$ScreeningId,
    [string]$RedisContainer = "meteor-redis"
)

$permitsKey = "grab:sem:permits:$ScreeningId"
$maxKey = "grab:sem:max:$ScreeningId"
$leaseKey = "grab:sem:lease:$ScreeningId"

Write-Host "== Meteor M1B-05 Semaphore Audit =="
Write-Host "screeningId=$ScreeningId"
Write-Host "container=$RedisContainer"

$lua = @'
local permits = tonumber(redis.call('GET', KEYS[1]) or '-1')
local maxv = tonumber(redis.call('GET', KEYS[2]) or '-1')
local t = redis.call('TIME')
local nowMs = tonumber(t[1]) * 1000 + math.floor(tonumber(t[2]) / 1000)
local leaseCount = redis.call('ZCARD', KEYS[3])
local expiredCount = redis.call('ZCOUNT', KEYS[3], '-inf', nowMs)
return {maxv, permits, leaseCount, expiredCount, nowMs}
'@

$result = docker exec $RedisContainer redis-cli --raw EVAL $lua 3 $permitsKey $maxKey $leaseKey
if ($LASTEXITCODE -ne 0) {
    throw "redis semaphore audit failed"
}

$values = @($result)
if ($values.Count -lt 5) {
    Write-Host $result
    throw "unexpected redis-cli output"
}

Write-Host ("max={0}" -f $values[0])
Write-Host ("permits={0}" -f $values[1])
Write-Host ("lease_count={0}" -f $values[2])
Write-Host ("expired_lease_count={0}" -f $values[3])
Write-Host ("redis_now_ms={0}" -f $values[4])

if ([int64]$values[0] -ge 0 -and [int64]$values[1] -ge 0) {
    $inflight = [int64]$values[0] - [int64]$values[1]
    Write-Host ("derived_inflight=max-permits={0}" -f $inflight)
}
