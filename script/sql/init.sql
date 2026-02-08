SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------
-- Create databases
-- -----------------------------
CREATE DATABASE IF NOT EXISTS meteor_user
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS meteor_admin
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS meteor_message
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS meteor_merchant
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS meteor_product
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

CREATE DATABASE IF NOT EXISTS meteor_ticketing
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;



CREATE DATABASE IF NOT EXISTS meteor_movie
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

-- =========================================================
-- meteor_user
-- =========================================================
USE meteor_user;

-- -----------------------------
-- user table
-- -----------------------------
DROP TABLE IF EXISTS user;
CREATE TABLE user (
                      id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                      username VARCHAR(50) NOT NULL COMMENT '用户名',
                      password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
                      phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                      avatar VARCHAR(500) DEFAULT NULL COMMENT '头像',
                      status TINYINT NOT NULL DEFAULT 0 COMMENT '用户状态：0-正常，1-禁用',
                      role TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-商家，2-管理',
                      last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
                      PRIMARY KEY (id),

                      UNIQUE INDEX uk_username_not_deleted (
                          username,
                          (CASE WHEN is_deleted = 0 THEN 0 ELSE NULL END)
                          ),

                      INDEX idx_username (username),
                      INDEX idx_phone_role (phone, role)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='用户表';


ALTER TABLE user
    ADD COLUMN preference_inited TINYINT NOT NULL DEFAULT 0
    COMMENT '偏好是否已初始化(0=否 1=是)';



DROP TABLE IF EXISTS user_category_preference;

CREATE TABLE user_category_preference (
                                          id            BIGINT PRIMARY KEY COMMENT '主键',
                                          user_id       BIGINT NOT NULL COMMENT '用户ID',
                                          category_id   BIGINT NOT NULL COMMENT '分类ID(来自movie模块)',
                                          source        TINYINT NOT NULL COMMENT '来源:1=manual 2=purchase 3=browse',
                                          score         INT NOT NULL DEFAULT 0 COMMENT '偏好分数(越大越喜欢)',
                                          last_seen_time DATETIME NULL COMMENT '最后一次触达时间(购买/浏览/手动更新)',

                                          create_time   DATETIME NOT NULL,
                                          update_time   DATETIME NOT NULL,

                                          UNIQUE KEY uk_user_category_source (user_id, category_id, source),
                                          KEY idx_user_score (user_id, score),
                                          KEY idx_user_update (user_id, update_time),
                                          KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户分类偏好(多来源)';


-- -----------------------------
-- merchant_apply table (user side)
-- -----------------------------
DROP TABLE IF EXISTS merchant_apply;
CREATE TABLE merchant_apply (
                                id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
                                user_id BIGINT NOT NULL COMMENT '用户ID',
                                shop_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
                                apply_reason VARCHAR(255) DEFAULT NULL COMMENT '申请理由',
                                status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-通过 2-拒绝',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (id),
                                INDEX idx_user_id (user_id),
                                INDEX idx_status (status),
                                INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='商家申请表（用户侧）';

-- FK: merchant_apply.user_id -> user.id（同库内 OK）
ALTER TABLE merchant_apply
    ADD CONSTRAINT fk_merchant_apply_user_id
        FOREIGN KEY (user_id) REFERENCES user(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

-- =========================================================
-- meteor_admin
-- =========================================================
USE meteor_admin;

DROP TABLE IF EXISTS merchant_apply;
CREATE TABLE merchant_apply (
                                id BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理端记录ID',
                                apply_id BIGINT NOT NULL COMMENT '用户模块的申请ID',
                                user_id BIGINT NOT NULL COMMENT '用户ID',
                                shop_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
                                apply_reason VARCHAR(255) DEFAULT NULL COMMENT '申请理由',
                                status TINYINT NOT NULL COMMENT '状态：0-PENDING 1-APPROVED 2-REJECTED',
                                reject_reason VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
                                reviewed_by BIGINT DEFAULT NULL COMMENT '审核人ID',
                                reviewed_time DATETIME DEFAULT NULL COMMENT '审核时间',
                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_apply_id (apply_id),
                                INDEX idx_status (status),
                                INDEX idx_user_id (user_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='商家申请表（管理端，审核视图）';

CREATE INDEX idx_shop_name ON merchant_apply(shop_name);
CREATE INDEX idx_user_status_shop ON merchant_apply(user_id, status, shop_name);

USE meteor_admin;

ALTER TABLE merchant_apply
    ADD COLUMN reviewed_msg_sent TINYINT NOT NULL DEFAULT 0
    COMMENT '审核结果消息是否已发送：0-未发送 1-已发送'
        AFTER reviewed_time,
    ADD COLUMN reviewed_msg_sent_time DATETIME DEFAULT NULL
        COMMENT '审核结果消息发送时间'
        AFTER reviewed_msg_sent;

CREATE INDEX idx_status_msgsent
    ON merchant_apply (status, reviewed_msg_sent);


-- 示例数据（可选，不要就删掉这一段）
INSERT INTO merchant_apply
(apply_id, user_id, shop_name, apply_reason, status, reject_reason, reviewed_by, reviewed_time)
VALUES
    (1001, 201, '喵星便利店', '想开店试试', 0, NULL, NULL, NULL),
    (1002, 202, '蓝天服饰', '扩大销售渠道', 1, NULL, 301, '2026-01-15 10:23:45'),
    (1003, 203, '小庞科技', '卖软件周边', 2, '资料不全', 302, '2026-01-16 11:00:12'),
    (1004, 204, '橙子甜品', '创业尝试', 0, NULL, NULL, NULL),
    (1005, 205, '星辰书店', '希望提供本地书籍', 1, NULL, 303, '2026-01-14 09:45:30'),
    (1006, 206, '风车咖啡', '爱好咖啡', 2, '店铺位置不合格', 304, '2026-01-17 14:12:20'),
    (1007, 207, '未来家居', '家居电商', 0, NULL, NULL, NULL),
    (1008, 208, '萌宠用品', '宠物相关', 1, NULL, 305, '2026-01-18 15:30:00'),
    (1009, 209, '奇趣玩具', '儿童玩具销售', 2, '审核信息错误', 306, '2026-01-19 16:45:10'),
    (1010, 210, '健康食品坊', '健康零食', 0, NULL, NULL, NULL),
    (1011, 211, '小庞影像', '摄影服务', 1, NULL, 307, '2026-01-20 13:20:05'),
    (1012, 212, '运动天地', '体育用品', 2, '资质不符', 308, '2026-01-21 11:50:33'),
    (1013, 213, '绿叶花店', '花卉销售', 0, NULL, NULL, NULL),
    (1014, 214, '梦想乐器', '乐器销售', 1, NULL, 309, '2026-01-22 10:10:10'),
    (1015, 215, '奇妙文具', '办公用品销售', 2, '资料不全', 310, '2026-01-23 09:05:55');

-- =========================================================
-- meteor_message
-- =========================================================
USE meteor_message;
DROP TABLE IF EXISTS user_message;
CREATE TABLE user_message (
                              id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              user_id BIGINT NOT NULL COMMENT '用户ID(消息归属)',
                              source TINYINT NOT NULL DEFAULT 0 COMMENT '消息来源：0系统消息 1业务事件(如MQ消费生成)',
                              type TINYINT NOT NULL COMMENT '消息类型(冗余字段，避免查询总是JOIN)',
                              title VARCHAR(64) DEFAULT NULL COMMENT '消息标题(冗余字段)',
                              content VARCHAR(512) DEFAULT NULL COMMENT '消息内容(冗余字段，短内容；长内容可改TEXT)',
                              biz_key VARCHAR(64) DEFAULT NULL COMMENT '业务幂等键(可选)，用于防重复写入，如: merchantApply:123',
                              read_status TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态：0未读 1已读',
                              read_time DATETIME DEFAULT NULL COMMENT '已读时间',
                              deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0正常 1已删除(软删)',
                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(投递/生成时间)',
                              PRIMARY KEY (id),

                              UNIQUE KEY uk_user_message_user_biz (user_id, biz_key),

                              KEY idx_user_message_inbox (user_id, deleted, read_status, id),
                              KEY idx_user_message_page (user_id, id),
                              KEY idx_user_message_biz_key (biz_key)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='用户消息表';

ALTER TABLE user_message
    ADD KEY idx_user_message_cleanup (deleted, create_time, id);




-- =========================================================
-- meteor_merchant
-- =========================================================

use meteor_merchant;

DROP TABLE IF EXISTS merchant;
CREATE TABLE merchant (
                          id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
                          user_id BIGINT NOT NULL COMMENT '关联用户ID（唯一）',

                          shop_name VARCHAR(100) NOT NULL COMMENT '店铺名称',
                          notice VARCHAR(255) DEFAULT NULL COMMENT '店铺公告/简介（短）',

                          status TINYINT NOT NULL DEFAULT 0 COMMENT '商家状态：0-正常 1-冻结 2-关闭',
                          verified_time DATETIME DEFAULT NULL COMMENT '审核通过/开通时间',

                          apply_id BIGINT DEFAULT NULL COMMENT '来源申请ID（用于幂等/追溯）',

                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                          is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '软删：0-未删 1-已删',

                          PRIMARY KEY (id),

                          UNIQUE KEY uk_user_not_deleted (
                              user_id,
                              (CASE WHEN is_deleted = 0 THEN 0 ELSE NULL END)
                              ),

                          UNIQUE KEY uk_apply_not_deleted (
                              apply_id,
                              (CASE WHEN is_deleted = 0 THEN 0 ELSE NULL END)
                              ),

                          INDEX idx_status (status),
                          INDEX idx_shop_name (shop_name),
                          UNIQUE KEY uk_merchant_user_id (user_id),
                          UNIQUE KEY uk_merchant_apply_id (apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';


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



-- =========================================================
-- meteor_movie
-- =========================================================

use meteor_movie;



DROP TABLE IF EXISTS movie;

CREATE TABLE movie (
                       id            BIGINT PRIMARY KEY COMMENT '电影ID',
                       title         VARCHAR(128) NOT NULL COMMENT '电影名称',
                       alias         VARCHAR(128) NULL COMMENT '别名/英文名',
                       intro         TEXT NULL COMMENT '简介',
                       duration_min  INT NULL COMMENT '时长(分钟)',
                       release_date  DATE NULL COMMENT '上映日期',
                       status        TINYINT NOT NULL DEFAULT 1 COMMENT '1=COMING 2=SHOWING 3=OFF',

                       create_time   DATETIME NOT NULL COMMENT '创建时间',
                       update_time   DATETIME NOT NULL COMMENT '更新时间',
                       create_by     BIGINT NULL COMMENT '创建人',
                       update_by     BIGINT NULL COMMENT '更新人',
                       deleted       TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

                       KEY idx_title (title),
                       KEY idx_status_release (status, release_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影信息表';

ALTER TABLE movie
    ADD UNIQUE KEY uk_movie_title_deleted (title, deleted);


DROP TABLE IF EXISTS movie_category ;
CREATE TABLE movie_category (
                                id          BIGINT PRIMARY KEY COMMENT '分类ID',
                                name        VARCHAR(64) NOT NULL COMMENT '分类名称',
                                sort        INT NOT NULL DEFAULT 0 COMMENT '排序',

                                create_time DATETIME NOT NULL COMMENT '创建时间',
                                update_time DATETIME NOT NULL COMMENT '更新时间',
                                create_by   BIGINT NULL COMMENT '创建人',
                                update_by   BIGINT NULL COMMENT '更新人',
                                deleted     TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

                                UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影分类表';

DROP TABLE IF EXISTS movie_category_rel;
CREATE TABLE movie_category_rel (
                                    id           BIGINT PRIMARY KEY COMMENT '主键',
                                    movie_id     BIGINT NOT NULL COMMENT '电影ID',
                                    category_id  BIGINT NOT NULL COMMENT '分类ID',

                                    create_time  DATETIME NOT NULL COMMENT '创建时间',
                                    update_time  DATETIME NOT NULL COMMENT '更新时间',
                                    create_by    BIGINT NULL COMMENT '创建人',
                                    update_by    BIGINT NULL COMMENT '更新人',
                                    deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

                                    UNIQUE KEY uk_movie_category (movie_id, category_id),
                                    KEY idx_category_movie (category_id, movie_id),
                                    KEY idx_movie (movie_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影分类关联表';


DROP TABLE IF EXISTS media_asset ;
CREATE TABLE media_asset (
                             id          BIGINT PRIMARY KEY COMMENT '资源ID',
                             biz_type    TINYINT NOT NULL COMMENT '1=movie 2=screening 3=cinema',
                             biz_id      BIGINT NOT NULL COMMENT '业务ID',
                             object_key          VARCHAR(512) NOT NULL COMMENT '图片路径',
                             kind        TINYINT NOT NULL DEFAULT 1 COMMENT '1=POSTER 2=COVER 3=GALLERY',
                             sort        INT NOT NULL DEFAULT 0 COMMENT '排序',

                             create_time DATETIME NOT NULL COMMENT '创建时间',
                             update_time DATETIME NOT NULL COMMENT '更新时间',
                             create_by   BIGINT NULL COMMENT '创建人',
                             update_by   BIGINT NULL COMMENT '更新人',
                             deleted     TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

                             KEY idx_object_key (object_key),
                             KEY idx_biz (biz_type, biz_id, kind, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片资源表';


set FOREIGN_KEY_CHECKS = 1;