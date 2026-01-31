package com.meteor.merchant.service.tx;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.meteor.merchant.domain.entity.Merchant;
import com.meteor.merchant.mapper.MerchantMapper;
import com.meteor.merchant.service.assembler.NewMerchantAssembler;
import com.meteor.mq.contract.merchant.MerchantApplyReviewedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

/**
 * @author Programmer
 * @date 2026-01-31 23:02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantActivatedTxService {

    private final MerchantMapper merchantMapper;

    private final NewMerchantAssembler newMerchantAssembler;

    @Transactional(rollbackFor = Exception.class)
    public void processApproved(MerchantApplyReviewedMessage msg) {

        LocalDateTime verifiedTime =
                msg.getReviewedTime() != null
                        ? msg.getReviewedTime()
                        : LocalDateTime.now();

        Merchant merchant = newMerchantAssembler.buildNewMerchant(msg, verifiedTime);

        try {
            merchantMapper.insert(merchant);
            log.info("merchant created, merchantId={}, userId={}, applyId={}",
                    merchant.getId(), msg.getUserId(), msg.getApplyId());
            return;
        } catch (DuplicateKeyException dup) {
            log.info("merchant already exists (duplicate key), userId={}, applyId={}",
                    msg.getUserId(), msg.getApplyId());
        }

        Merchant db = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, msg.getUserId())
                .last("limit 1"));

        if (db == null) {
            throw new IllegalStateException(
                    "duplicate key but merchant not found, userId=" + msg.getUserId());
        }

        boolean updated = patchIfNecessary(db, msg, verifiedTime);

        if (updated) {
            db.setUpdateTime(LocalDateTime.now());
            merchantMapper.updateById(db);
            log.info("merchant updated by idempotent patch, merchantId={}, userId={}, applyId={}",
                    db.getId(), msg.getUserId(), msg.getApplyId());
        } else {
            log.info("merchant already processed, no patch needed, merchantId={}, userId={}, applyId={}",
                    db.getId(), msg.getUserId(), msg.getApplyId());
        }
    }

    private boolean patchIfNecessary(
            Merchant db,
            MerchantApplyReviewedMessage msg,
            LocalDateTime verifiedTime
    ) {
        boolean changed = false;

        if (db.getApplyId() == null) {
            db.setApplyId(msg.getApplyId());
            changed = true;
        }

        if (db.getVerifiedTime() == null) {
            db.setVerifiedTime(verifiedTime);
            changed = true;
        }

        if (db.getShopName() == null || db.getShopName().isBlank()) {
            db.setShopName(msg.getShopName());
            changed = true;
        }

        return changed;
    }

}
