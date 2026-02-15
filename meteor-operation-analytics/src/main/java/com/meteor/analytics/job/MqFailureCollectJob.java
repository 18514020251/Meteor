package com.meteor.analytics.job;

import com.meteor.analytics.domain.entity.OpMqFailMsg;
import com.meteor.analytics.service.IOpMqFailMsgService;
import com.meteor.api.common.dtp.RemoteMqFailureDTO;
import com.meteor.api.contract.mqfail.client.AdminMqFailureClient;
import com.meteor.api.contract.mqfail.client.MerchantMqFailureClient;
import com.meteor.api.contract.mqfail.client.UserMqFailureClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqFailureCollectJob {

    private final MerchantMqFailureClient merchantClient;
    private final UserMqFailureClient userClient;
    private final AdminMqFailureClient adminClient;

    private final IOpMqFailMsgService opMqFailMsgService;

    private final Executor executor = Executors.newFixedThreadPool(6);

    @Scheduled(cron = "0 */15 * * * ?")
    public void collect() {
        log.info("[MQ失败消息，发起远程调用]");

        CompletableFuture<List<RemoteMqFailureDTO>> fMerchant =
                CompletableFuture.supplyAsync(merchantClient::recentFailed, executor)
                        .exceptionally(ex -> {
                            log.error("[mq-fail-collect] source=MERCHANT fetch failed: {}", ex.getMessage(), ex);
                            return Collections.emptyList();
                        });

        CompletableFuture<List<RemoteMqFailureDTO>> fUser =
                CompletableFuture.supplyAsync(userClient::recentFailed, executor)
                        .exceptionally(ex -> {
                            log.error("[mq-fail-collect] source=USER fetch failed: {}", ex.getMessage(), ex);
                            return Collections.emptyList();
                        });

        CompletableFuture<List<RemoteMqFailureDTO>> fAdmin =
                CompletableFuture.supplyAsync(adminClient::recentFailed, executor)
                        .exceptionally(ex -> {
                            log.error("[mq-fail-collect] source=ADMIN fetch failed: {}", ex.getMessage(), ex);
                            return Collections.emptyList();
                        });

        CompletableFuture.allOf(fMerchant, fUser, fAdmin).join();

        persist("MERCHANT", fMerchant.join());
        persist("USER", fUser.join());
        persist("ADMIN", fAdmin.join());
    }

    private void persist(String sourceModule, List<RemoteMqFailureDTO> list) {
        long start = System.currentTimeMillis();
        if (list == null || list.isEmpty()) {
            log.info("[mq-fail-collect] source={} empty cost={}ms",
                    sourceModule, System.currentTimeMillis() - start);
            return;
        }

        int ok = 0;
        for (RemoteMqFailureDTO dto : list) {
            OpMqFailMsg e = map(sourceModule, dto);
            opMqFailMsgService.upsertByUk(e);
            ok++;
        }

        log.info("[mq-fail-collect] source={} fetched={} upsertOk={} cost={}ms",
                sourceModule, list.size(), ok, System.currentTimeMillis() - start);
    }

    private OpMqFailMsg map(String sourceModule, RemoteMqFailureDTO d) {
        OpMqFailMsg e = new OpMqFailMsg();

        e.setSourceModule(sourceModule);
        e.setMsgId(d.getMsgId());
        e.setBizId(d.getBizId());
        e.setExchangeName(d.getExchangeName());
        e.setRoutingKey(d.getRoutingKey());
        e.setTopic(d.getTopic());
        e.setPayload(d.getPayload());

        e.setStatus(d.getStatus() == null ? 0 : d.getStatus().getCode());
        e.setRetryCnt(d.getRetryCnt() == null ? 0 : d.getRetryCnt());
        e.setNextRetryTime(d.getNextRetryTime());

        e.setLastError(d.getLastError());

        e.setSourceCreateTime(d.getCreateTime());
        e.setSourceUpdateTime(d.getUpdateTime());
        return e;
    }
}
