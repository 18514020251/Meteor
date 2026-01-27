package com.meteor.user.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.common.mq.constants.MqConstants;
import com.meteor.common.mq.merchant.MerchantApplyEvent;
import com.meteor.common.mq.merchant.MerchantApplyCreatedMessage;
import com.meteor.mq.core.MqSendResult;
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
@SuppressWarnings("squid:S1123")
public class MerchantApplyEventPublisher {

    private final MqSender mqSender;
    private final MerchantApplyMessageAssembler assembler;


    /**
     *  @Deprecated (since = "use publishCreatedOrThrow instead" )
     * */
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
                MqConstants.CONFIRM_TIMEOUT
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

