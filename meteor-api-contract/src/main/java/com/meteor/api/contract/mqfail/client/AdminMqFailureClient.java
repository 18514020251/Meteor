package com.meteor.api.contract.mqfail.client;

import com.meteor.api.common.dto.RemoteMqFailureDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 *  管理端MQ失败消息服务客户端
 *
 * @author Programmer
 * @version 1.0
 * @date 2026-02-13 18:03
 */
@FeignClient(name = "admin-mq-failure", url = "${mqfail.remote.admin}")
public interface AdminMqFailureClient {

    @GetMapping("/internal/admin/mqFailures/recentFailed")
    List<RemoteMqFailureDTO> recentFailed();
}
