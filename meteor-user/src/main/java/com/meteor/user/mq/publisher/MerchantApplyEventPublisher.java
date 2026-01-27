package com.meteor.user.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.mq.merchant.MerchantApplyEvent;
import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.mq.assembler.MerchantApplyMessageAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 商家申请事件发布器
 * @author Programmer
 */
@Component
@RequiredArgsConstructor
public class MerchantApplyEventPublisher {

    private final MqSender mqSender;
    private final MerchantApplyMessageAssembler assembler;

    public void publishCreated(MerchantApply apply) {

        MerchantApplyCreatedMessage message = assembler.from(apply);

        mqSender.send(
                MerchantApplyEvent.Exchange.MERCHANT_APPLY,
                MerchantApplyEvent.RoutingKey.MERCHANT_APPLY_CREATED,
                message
        );
    }

    public void publishCreatedOrThrow(MerchantApply apply) {

        MerchantApplyCreatedMessage message = assembler.from(apply);

        MqSendResult result = mqSender.sendAndWaitConfirm(
                MerchantApplyEvent.Exchange.MERCHANT_APPLY,
                MerchantApplyEvent.RoutingKey.MERCHANT_APPLY_CREATED,
                message,
                Duration.ofSeconds(3)
        );

        if (!result.isAck()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                    "MQ confirm failed");
        }

        if (result.noRoute()) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR,
                    "MQ NO_ROUTE");
        }
    }
}

