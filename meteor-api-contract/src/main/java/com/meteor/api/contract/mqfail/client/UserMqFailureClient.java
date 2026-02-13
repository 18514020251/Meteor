package com.meteor.api.contract.mqfail.client;

import com.meteor.api.common.dtp.RemoteMqFailureDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 *  用户MQ失败消息服务客户端
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 18:03
 */
@FeignClient(name = "user-mq-failure", url = "${mqfail.remote.user}")
public interface UserMqFailureClient {

    @GetMapping("/internal/user/mqFailures/recentFailed")
    List<RemoteMqFailureDTO> recentFailed();
}
