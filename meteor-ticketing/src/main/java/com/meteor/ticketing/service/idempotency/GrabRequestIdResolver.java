package com.meteor.ticketing.service.idempotency;

import com.meteor.common.cache.RedisKeyConstants;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.id.utils.SnowflakeIdGenerator;
import com.meteor.ticketing.redis.RedisScripts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 *  抢票请求ID解析器
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-13
 */
@Component
@RequiredArgsConstructor
public class GrabRequestIdResolver {

    private final StringRedisTemplate redisTemplate;
    private final SnowflakeIdGenerator idGenerator;


    private static final String CONFLICT = "__CONFLICT__";

    private static final Duration REQUEST_TTL = Duration.ofHours(24);

    /**
     *  解析抢票请求ID
     *
     *  @param userId 用户ID
     *  @param screeningId 屏幕ID
     *  @param clientRequestId 客户端请求ID
     *  @param quantity  quantity量
     *  @return  抢票请求ID
     * */
    public String resolve(Long userId, Long screeningId, String clientRequestId, int quantity) {
        String candidateRequestId = String.valueOf(idGenerator.nextId());

        String fingerprint = buildFingerprint(screeningId, quantity);

        String requestKey = RedisKeyConstants.buildGrabRequestKey(userId, clientRequestId);

        String result = redisTemplate.execute(
                RedisScripts.RESOLVE_GRAB_REQUEST_ID,
                List.of(requestKey),
                candidateRequestId,
                fingerprint,
                String.valueOf(REQUEST_TTL.toMillis())
        );

        if (CONFLICT.equals(result)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "clientRequestId 与原请求参数冲突");
        }

        if (result == null || result.isBlank()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "解析抢票请求身份失败");
        }

        return result;
    }

    private String buildFingerprint(
            Long screeningId,
            int quantity
    ) {
        return "screeningId="
                + screeningId
                + "&quantity="
                + quantity;
    }
}
