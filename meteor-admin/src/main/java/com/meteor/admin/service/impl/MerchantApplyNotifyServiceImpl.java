package com.meteor.admin.service.impl;

import com.meteor.admin.mq.publisher.MessagePublisher;
import com.meteor.admin.service.assembler.MerchantApplyNotifyMqAssembler;
import com.meteor.admin.domain.entity.MerchantApply;
import com.meteor.admin.service.IMerchantApplyNotifyService;
import com.meteor.mq.contract.message.UserEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 *  商家申请审核结果通知服务实现类
 *
 * @author Programmer
 * @date 2026-01-30 21:44
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantApplyNotifyServiceImpl implements IMerchantApplyNotifyService {

    private final MerchantApplyNotifyMqAssembler assembler;
    private final MessagePublisher messagePublisher;

    @Override
    public void notifyApproved(MerchantApply apply) {
        if (apply == null) {
            return;
        }
        UserEventMessage msg = assembler.toApprovedNotifyMessage(apply);
        messagePublisher.sendNotify(apply, msg, "notifyApproved");
    }

    @Override
    public void notifyRejected(MerchantApply apply) {
        if (apply == null) {
            return;
        }
        UserEventMessage msg = assembler.toRejectedNotifyMessage(apply);
        messagePublisher.sendNotify(apply, msg, "notifyRejected");
    }


}


