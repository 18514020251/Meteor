CREATE TABLE ticket_inventory_reservation (
    reservation_id     VARCHAR(64) NOT NULL COMMENT '库存预留业务ID，当前直接复用 requestId',

    client_request_id  VARCHAR(64) NOT NULL COMMENT '客户端请求幂等ID',
    screening_id       BIGINT NOT NULL COMMENT '场次ID',
    user_id            BIGINT NOT NULL COMMENT '用户ID',
    quantity           INT NOT NULL COMMENT '预留票数',
    status             VARCHAR(32) NOT NULL COMMENT 'PRE_RESERVED / CONFIRMED / RELEASED / COMPENSATED',
    expire_at          DATETIME(3) NULL COMMENT '业务过期时间，当前阶段暂不赋值',

    created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)COMMENT '创建时间',
    updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (reservation_id),
    UNIQUE KEY uk_user_client_request (user_id, client_request_id),
    KEY idx_screening_status (screening_id, status),
    KEY idx_status_expire (status, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票务库存预留表';

