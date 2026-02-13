package com.meteor.user.mq.publisher;

import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyCreatedMessage;
import com.meteor.user.domain.cmd.MqSendCmd;
import com.meteor.user.domain.entity.MerchantApply;
import com.meteor.user.mq.assembler.MerchantApplyMessageAssembler;
import com.meteor.user.mq.support.UserMqSendGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * 商家申请事件发布器
 * @author Programmer
 */
@RequiredArgsConstructor
@Component
public class MerchantApplyEventPublisher {

    private final MerchantApplyMessageAssembler assembler;
    private final UserMqSendGuard mqGuard;

    public void publishCreatedOrThrow(MerchantApply apply) {

        MerchantApplyCreatedMessage message = assembler.from(apply);

        mqGuard.send(new MqSendCmd(
                "user_apply_" + apply.getId(),
                ModuleEnum.USER,
                apply.getId(),
                MerchantApplyContract.Exchange.MERCHANT_APPLY,
                MerchantApplyContract.RoutingKey.MERCHANT_APPLY_CREATED,
                "merchant_apply_created",
                message,
                MerchantApplyContract.CONFIRM_TIMEOUT,
                false
        ));
    }
}


