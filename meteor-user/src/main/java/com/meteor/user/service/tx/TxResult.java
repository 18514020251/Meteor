package com.meteor.user.service.tx;

import com.meteor.common.enums.merchant.MerchantApplyStatusEnum;
import lombok.Getter;

/**
 * 事务执行结果（仅描述 DB 事实）
 * @author Programmer
 */
@Getter
public class TxResult {

    public enum State {
        UPDATED,
        ALREADY_PROCESSED,
        NOT_FOUND,
        ERROR
    }

    private final State state;
    private final MerchantApplyStatusEnum status;

    private TxResult(State state, MerchantApplyStatusEnum status) {
        this.state = state;
        this.status = status;
    }

    public static TxResult updated(MerchantApplyStatusEnum status) {
        return new TxResult(State.UPDATED, status);
    }

    public static TxResult alreadyProcessed() {
        return new TxResult(State.ALREADY_PROCESSED, null);
    }

    public static TxResult notFound() {
        return new TxResult(State.NOT_FOUND, null);
    }

    public static TxResult error() {
        return new TxResult(State.ERROR, null);
    }

    public boolean isUpdated() {
        return state == State.UPDATED;
    }

    public boolean isApproved() {
        return isUpdated() && status == MerchantApplyStatusEnum.APPROVED;
    }
}
