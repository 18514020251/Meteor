package com.meteor.user.mq.publisher;

import com.meteor.common.mq.merchant.MerchantApplyEvent;
import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import com.meteor.mq.core.MqSender;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.mq.assembler.MerchantApplyMessageAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}

