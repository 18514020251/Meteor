package com.meteor.user.mq.publisher;

import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyCreatedMessage;
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
public class MerchantApplyEventPublisher {

    private final MqSender mqSender;
    private final MerchantApplyMessageAssembler assembler;


    /**
     *  @Deprecated (since = "use publishCreatedOrThrow instead" )
     * */
    public void publishCreated(MerchantApply apply) {

        MerchantApplyCreatedMessage message = assembler.from(apply);

        mqSender.send(
                MerchantApplyContract.Exchange.MERCHANT_APPLY,
                MerchantApplyContract.RoutingKey.MERCHANT_APPLY_CREATED,
                message
        );
    }

    public void publishCreatedOrThrow(MerchantApply apply) {

        MerchantApplyCreatedMessage message = assembler.from(apply);

        MqSendResult result = mqSender.sendAndWaitConfirm(
                MerchantApplyContract.Exchange.MERCHANT_APPLY,
                MerchantApplyContract.RoutingKey.MERCHANT_APPLY_CREATED,
                message,
                MerchantApplyContract.CONFIRM_TIMEOUT
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

