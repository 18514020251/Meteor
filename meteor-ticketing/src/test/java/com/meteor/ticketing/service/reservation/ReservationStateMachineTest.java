package com.meteor.ticketing.service.reservation;

import com.meteor.ticketing.enums.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static com.meteor.ticketing.enums.ReservationStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Reservation 状态转换规则测试。
 *
 * @author 昭兮
 * @version 1.0
 * @date 2026-08-15
 */
class ReservationStateMachineTest {

    private final ReservationStateMachine stateMachine = new ReservationStateMachine();

    @DisplayName("PRE_RESERVED 可以正常转换为 CONFIRMED、RELEASED、COMPENSATED")
    @ParameterizedTest
    @EnumSource(
            value = ReservationStatus.class,
            names = {"CONFIRMED", "RELEASED", "COMPENSATED"}
    )
    void preReservedShouldTransitionToAllowedTerminalStatus(ReservationStatus target) {
        ReservationStatus result = stateMachine.transition(PRE_RESERVED, target);

        assertThat(result).isEqualTo(target);
    }

    @DisplayName("CONFIRMED、RELEASED、COMPENSATED 状态是终态，转换到自身时幂等")
    @ParameterizedTest
    @EnumSource(
            value = ReservationStatus.class,
            names = {"CONFIRMED", "RELEASED", "COMPENSATED"}
    )
    void terminalStatusShouldBeIdempotent(ReservationStatus status) {
        ReservationStatus result = stateMachine.transition(status, status);

        assertThat(result).isEqualTo(status);
    }

    @DisplayName("终态之间不能相互转换")
    @ParameterizedTest
    @CsvSource({
            "CONFIRMED, RELEASED",
            "CONFIRMED, COMPENSATED",
            "RELEASED, CONFIRMED",
            "RELEASED, COMPENSATED",
            "COMPENSATED, CONFIRMED",
            "COMPENSATED, RELEASED"
    })
    void terminalStatusShouldRejectTransitionToAnotherTerminalStatus(
            ReservationStatus current,
            ReservationStatus target
    ) {
        assertThatThrownBy(() -> stateMachine.transition(current, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(current + " -> " + target);
    }

    @DisplayName("当前状态为 null 时应抛出异常")
    @Test
    void shouldRejectNullCurrentStatus() {
        assertThatThrownBy(() -> stateMachine.transition(null, CONFIRMED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("当前预约状态不能为空");
    }

    @DisplayName("目标状态为 null 时应抛出异常")
    @Test
    void shouldRejectNullTargetStatus() {
        assertThatThrownBy(() -> stateMachine.transition(PRE_RESERVED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("目标预约状态不能为空");
    }

    @DisplayName("PRE_RESERVED 转换到自身时幂等")
    @Test
    void preReservedShouldBeIdempotent() {
        ReservationStatus result = stateMachine.transition(PRE_RESERVED, PRE_RESERVED);

        assertThat(result).isEqualTo(PRE_RESERVED);
    }
}