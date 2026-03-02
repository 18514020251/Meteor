package com.meteor.admin.mq.publisher;

import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.mq.assembler.MerchantApplyMqAssemblerAssembler;
import com.meteor.admin.mq.support.AdminMqFailMsgBuilder;
import com.meteor.admin.service.IMqFailMsgService;
import com.meteor.api.model.AdminMqFailureEntity;
import com.meteor.common.enums.system.ModuleEnum;
import com.meteor.common.exception.BizException;
import com.meteor.common.exception.CommonErrorCode;
import com.meteor.mq.contract.merchant.MerchantApplyContract;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import com.meteor.mq.core.MqSendResult;
import com.meteor.mq.core.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 *
 *  商家申请审核结果发布者
 *
 * @author Programmer
 * */
@Component
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyReviewedPublishe {

    private final MerchantApplyMqAssemblerAssembler merchantApplyAssembler;
    private final MqSender mqSender;
    private final IMqFailMsgService mqFailMsgService;
    private final AdminMqFailMsgBuilder failMsgBuilder;

    public void send(MerchantApply apply) {
        doSend(apply);
    }

    public void send(MerchantApply apply, Runnable onSuccess) {
        MqSendResult result = doSend(apply);
        if (result == null) {
            return;
        }

        if (result.isAck() && onSuccess != null) {
            try {
                onSuccess.run();
            } catch (Exception e) {
                log.warn("MQ sent ok but onSuccess failed, applyId={}", apply.getApplyId(), e);
                throw new BizException(CommonErrorCode.SYSTEM_ERROR);
            }
        }
    }

    private MqSendResult doSend(MerchantApply apply) {

        MerchantApplyReviewedMessage message =
                merchantApplyAssembler.toReviewedMessage(apply);

        MqSendResult result = mqSender.sendAndWaitConfirm(
                //"故意不存在(Admin侧)",
                MerchantApplyContract.Exchange.MERCHANT_APPLY,
                MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED,
                message,
                MerchantApplyContract.CONFIRM_TIMEOUT
        );

        if (result == null || !result.isAck() || result.noRoute()) {

            String error;

            if (result == null) {
                error = "MQ sendAndWaitConfirm returned null";
            } else if (result.noRoute()) {
                error = "MQ NO_ROUTE";
            } else {
                error = "MQ confirm failed";
            }
            log.error("MerchantApplyReviewed MQ send failed, applyId={}, error={}",
                    apply.getApplyId(), error);

            AdminMqFailureEntity failMsg = failMsgBuilder.build(
                    new AdminMqFailMsgBuilder.BuildCmd(
                            "apply_" + apply.getApplyId(),
                            ModuleEnum.APPLY,
                            apply.getApplyId(),
                            MerchantApplyContract.Exchange.MERCHANT_APPLY,
                            MerchantApplyContract.RoutingKey.MERCHANT_APPLY_REVIEWED,
                            "merchant_apply",
                            message,
                            error
                    )
            );

            mqFailMsgService.save(failMsg);
        }

        return result;
    }
}

