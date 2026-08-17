-- =========================================================
-- meteor_ticketing
-- =========================================================
use meteor_ticketing;

DROP TABLE IF EXISTS screening;
CREATE TABLE screening (
        id                BIGINT PRIMARY KEY COMMENT '场次ID',
        merchant_id       BIGINT NOT NULL COMMENT '商家ID(影院方/售票方)',
        movie_id          BIGINT NOT NULL COMMENT '电影ID',
        start_time        DATETIME NOT NULL COMMENT '开始时间',
        end_time          DATETIME NULL COMMENT '结束时间',
        sale_start_time   DATETIME NOT NULL COMMENT '开售时间',
        sale_end_time     DATETIME NULL COMMENT '停售时间',
        status            TINYINT NOT NULL DEFAULT 1 COMMENT '1=SCHEDULED 2=SELLING 3=SOLD_OUT 4=CLOSED 5=CANCELED',
        sale_mode         TINYINT NOT NULL DEFAULT 1 COMMENT '1=AUTO抢票 2=MANUAL选座 3=MIXED',
        base_price        INT NOT NULL COMMENT '基础价格(分)',
        min_price         INT NOT NULL COMMENT '最小价格(分)',
        max_price         INT NOT NULL COMMENT '最大价格(分)',
        total_tickets     INT NOT NULL COMMENT '总票数',
        available_tickets INT NOT NULL COMMENT '可用票数',
        sold_tickets      INT NOT NULL DEFAULT 0 COMMENT '已售票数',
        hot_score         BIGINT NOT NULL DEFAULT 0 COMMENT '热度分(展示用)',
        version           INT NOT NULL DEFAULT 0 COMMENT '版本号(防超卖)',
        create_time       DATETIME NOT NULL COMMENT '创建时间',
        update_time       DATETIME NOT NULL COMMENT '更新时间',
        create_by         BIGINT NULL COMMENT '创建人',
        update_by         BIGINT NULL COMMENT '更新人',
        deleted           TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',
        KEY idx_merchant_time (merchant_id, start_time),
        KEY idx_movie_time (movie_id, start_time),
        KEY idx_sale_status_time (status, sale_start_time, start_time),
        KEY idx_hot (hot_score),
        KEY idx_price (min_price, max_price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影场次表';

ALTER TABLE screening
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '场次ID';

DELETE FROM mq_outbox_event
WHERE routing_key = 'ticket.order.db_reserved';

drop table if exists mq_outbox_event;
CREATE TABLE mq_outbox_event (
        id              BIGINT      NOT NULL PRIMARY KEY,
        biz_key         VARCHAR(64)  NOT NULL,
        event_type      VARCHAR(64)  NOT NULL,
        exchange_name   VARCHAR(128) NOT NULL,
        routing_key     VARCHAR(128) NOT NULL,
        payload         MEDIUMTEXT   NOT NULL,
        status          INT          NOT NULL,
        retry_cnt       INT          NOT NULL DEFAULT 0,
        next_retry_time DATETIME(3)  NOT NULL,
        deliver_at      DATETIME(3)  NOT NULL,
        biz_expire_at   DATETIME(3)  NOT NULL,
        trace_id        VARCHAR(64)  NULL,
        last_error      VARCHAR(512) NULL,
        created_at      DATETIME(3)  NOT NULL,
        updated_at      DATETIME(3)  NOT NULL,
        UNIQUE KEY uk_event_dedupe (event_type, biz_key),
        KEY idx_outbox_scan (status, next_retry_time, deliver_at),
        KEY idx_outbox_expire (status, biz_expire_at),
        KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS hot_rank;
CREATE TABLE hot_rank (
        id            BIGINT PRIMARY KEY COMMENT '主键',
        period        TINYINT NOT NULL COMMENT '1=DAY 2=WEEK 3=MONTH',
        stat_date     DATE NOT NULL COMMENT '统计周期起始日',
        screening_id  BIGINT NOT NULL COMMENT '场次ID',
        score         BIGINT NOT NULL DEFAULT 0 COMMENT '热度分',
        sold_cnt      INT NOT NULL DEFAULT 0 COMMENT '售出票数',
        order_cnt     INT NOT NULL DEFAULT 0 COMMENT '订单数',

        create_time   DATETIME NOT NULL COMMENT '创建时间',
        update_time   DATETIME NOT NULL COMMENT '更新时间',
        create_by     BIGINT NULL COMMENT '创建人',
        update_by     BIGINT NULL COMMENT '更新人',
        deleted       TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

        UNIQUE KEY uk_period_date_item (period, stat_date, screening_id),
        KEY idx_period_date_score (period, stat_date, score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场次热度榜';


CREATE TABLE ticket_mq_consume_log (
        id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        msg_key      VARCHAR(64) NOT NULL COMMENT '消息唯一键(orderNo/eventId)',
        topic        VARCHAR(64) NOT NULL COMMENT '消息主题(ticket.order.create)',
        create_time  DATETIME NOT NULL COMMENT '创建时间',

        UNIQUE KEY uk_topic_msg (topic, msg_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消费去重表';
