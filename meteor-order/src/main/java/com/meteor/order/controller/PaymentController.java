package com.meteor.order.controller;


import com.meteor.common.result.Result;
import com.meteor.order.controller.dto.pay.PayConfirmRequest;
import com.meteor.order.controller.dto.pay.PayCreateRequest;
import com.meteor.order.controller.vo.pay.PayCreateVO;
import com.meteor.order.controller.vo.pay.PayStatusVO;
import com.meteor.order.enums.PayChannelEnum;
import com.meteor.order.service.IPaymentService;
import com.meteor.satoken.context.LoginContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 支付记录表 前端控制器
 * </p>
 *
 * @author Programmer
 * @since 2026-02-09
 */
@RestController
@RequestMapping("/order/pay")
@RequiredArgsConstructor
@Tag( name = "支付接口" )
public class PaymentController {

    private final IPaymentService payService;
    private final LoginContext loginContext;

    @Operation(summary = "创建支付单")
    @PostMapping("/create")
    public Result<PayCreateVO> create(@Valid @RequestBody PayCreateRequest req) {

        Long uid = loginContext.currentLoginId();

        PayCreateVO vo = payService.createPay(
                req.getOrderNo(),
                PayChannelEnum.of(req.getChannel()),
                uid
        );

        return Result.success(vo);
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/status/{payNo}")
    public Result<PayStatusVO> status(@PathVariable String payNo) {
        return Result.success(payService.getPayStatus(payNo));
    }

    @Operation(summary = "模拟支付成功")
    @PostMapping("/confirm")
    public Result<Boolean> confirm(@Valid @RequestBody PayConfirmRequest req) {

        boolean ok = payService.confirmPay(
                req.getPayNo(),
                req.getPayPwd(),
                req.getUid()
        );

        return Result.success(ok);
    }

}
