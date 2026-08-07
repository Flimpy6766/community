-- ============================================
-- community 内容社区 建库建表脚本
-- 执行方式: sudo mysql < init.sql  (Arch 默认 root 走 unix_socket 认证)
-- 或: mysql -u root -p < init.sql
-- ============================================

CREATE DATABASE IF NOT EXISTS community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE community;

-- 1. 用户表
CREATE TABLE `user` (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL COMMENT '登录名',
    password    VARCHAR(100) NOT NULL COMMENT 'Bcrypt 密文',
    nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    email       VARCHAR(100) DEFAULT NULL,
    bio         VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT ='用户表';

-- 2. 文章表
CREATE TABLE article (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT UNSIGNED NOT NULL COMMENT '作者ID',
    title          VARCHAR(100) NOT NULL COMMENT '标题',
    summary        VARCHAR(255) DEFAULT NULL COMMENT '摘要',
    content        LONGTEXT COMMENT '正文',
    cover          VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    status         TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
    view_count     INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    like_count     INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    favorite_count INT          NOT NULL DEFAULT 0 COMMENT '收藏数',
    comment_count  INT          NOT NULL DEFAULT 0 COMMENT '评论数',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_status_create (status, create_time)
) ENGINE = InnoDB COMMENT ='文章表';

-- 3. 标签表
CREATE TABLE tag (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30) NOT NULL COMMENT '标签名',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE = InnoDB COMMENT ='标签表';

-- 4. 文章-标签关联表 (多对多)
CREATE TABLE article_tag (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT UNSIGNED NOT NULL,
    tag_id     BIGINT UNSIGNED NOT NULL,
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    KEY idx_tag_id (tag_id)
) ENGINE = InnoDB COMMENT ='文章-标签关联表';

-- 5. 评论表
CREATE TABLE comment (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    article_id  BIGINT UNSIGNED NOT NULL COMMENT '所属文章',
    user_id     BIGINT UNSIGNED NOT NULL COMMENT '评论人',
    parent_id   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=顶级评论, 非0=回复的评论ID',
    content     VARCHAR(2000) NOT NULL COMMENT '评论内容',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_article_id (article_id),
    KEY idx_parent_id (parent_id)
) ENGINE = InnoDB COMMENT ='评论表';

-- 6. 点赞记录表
CREATE TABLE user_like (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NOT NULL,
    article_id  BIGINT UNSIGNED NOT NULL,
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_article (user_id, article_id)
) ENGINE = InnoDB COMMENT ='点赞记录表(防重复点赞)';

-- 7. 收藏表
CREATE TABLE favorite (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT UNSIGNED NOT NULL,
    article_id  BIGINT UNSIGNED NOT NULL,
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_article (user_id, article_id)
) ENGINE = InnoDB COMMENT ='收藏表';
