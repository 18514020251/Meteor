package com.meteor.common.utils;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 *  IP 工具类
 *
 * @author Programmer
 * @date 2026-01-22 17:56
 */
public class IpUtils {

    public static String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}

