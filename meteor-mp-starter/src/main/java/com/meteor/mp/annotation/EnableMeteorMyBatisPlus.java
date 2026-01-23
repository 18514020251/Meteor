package com.meteor.mp.annotation;

import com.meteor.mp.config.MyBatisPlusAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *  启用 Meteor MyBatisPlus
 *
 * @author Programmer
 * @date 2026-01-23 16:08
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MyBatisPlusAutoConfiguration.class)
public @interface EnableMeteorMyBatisPlus {
}
