-- ========================================
-- 音乐播放器后端服务数据库设计脚本
-- 版本: 1.0.0
-- 数据库: MySQL 9.4.0
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- 创建时间: 2025-09-01
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `music_server`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `music_server`;

-- ========================================
-- 1. 用户管理相关表
-- ========================================

-- 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
    `username`        VARCHAR(50)  NOT NULL COMMENT '用户名，唯一标识',
    `email`           VARCHAR(100) NOT NULL COMMENT '用户邮箱地址',
    `phone`           VARCHAR(20) COMMENT '用户手机号码',
    `password`        VARCHAR(255) NOT NULL COMMENT '用户密码，BCrypt加密存储',
    `nickname`        VARCHAR(100) COMMENT '用户昵称',
    `avatar`          VARCHAR(500) COMMENT '用户头像URL地址',
    `gender`          TINYINT   DEFAULT 0 COMMENT '用户性别：0-未知，1-男，2-女',
    `birthday`        DATE COMMENT '用户生日',
    `signature`       VARCHAR(500) COMMENT '用户个性签名',
    `level`           INT       DEFAULT 1 COMMENT '用户等级，默认为1级',
    `experience`      BIGINT    DEFAULT 0 COMMENT '用户经验值',
    `status`          TINYINT   DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常，2-冻结',
    `last_login_time` TIMESTAMP COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(45) COMMENT '最后登录IP地址',
    `created_time`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '用户创建时间',
    `updated_time`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '用户信息更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_level` (`level`),
    KEY `idx_created_time` (`created_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户基本信息表';

-- 用户配置表
DROP TABLE IF EXISTS `user_profiles`;
CREATE TABLE `user_profiles`
(
    `id`                 BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID，主键',
    `user_id`            BIGINT NOT NULL COMMENT '用户ID，外键关联users表',
    `music_preference`   JSON COMMENT '音乐偏好设置，JSON格式存储',
    `auto_play`          TINYINT   DEFAULT 1 COMMENT '自动播放设置：0-关闭，1-开启',
    `quality_preference` TINYINT   DEFAULT 1 COMMENT '音质偏好：1-标准，2-高品质，3-无损',
    `playback_mode`      TINYINT   DEFAULT 1 COMMENT '播放模式：1-顺序播放，2-随机播放，3-单曲循环',
    `volume`             INT       DEFAULT 80 COMMENT '默认音量大小，范围0-100',
    `privacy_level`      TINYINT   DEFAULT 1 COMMENT '隐私级别：1-公开，2-好友可见，3-私密',
    `created_time`       TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_user_profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户个性化配置表';

-- ========================================
-- 2. 音乐内容管理相关表
-- ========================================

-- 艺术家表
DROP TABLE IF EXISTS `artists`;
CREATE TABLE `artists`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '艺术家ID，主键',
    `name`            VARCHAR(100) NOT NULL COMMENT '艺术家姓名',
    `avatar`          VARCHAR(500) COMMENT '艺术家头像URL',
    `description`     TEXT COMMENT '艺术家描述信息',
    `country`         VARCHAR(50) COMMENT '艺术家所属国家',
    `birth_date`      DATE COMMENT '艺术家出生日期',
    `genre`           VARCHAR(100) COMMENT '主要音乐风格',
    `followers_count` BIGINT    DEFAULT 0 COMMENT '关注者数量',
    `status`          TINYINT   DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_time`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='艺术家信息表';

-- 专辑表
DROP TABLE IF EXISTS `albums`;
CREATE TABLE `albums`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '专辑ID，主键',
    `name`         VARCHAR(200) NOT NULL COMMENT '专辑名称',
    `cover`        VARCHAR(500) COMMENT '专辑封面图片URL',
    `artist_id`    BIGINT       NOT NULL COMMENT '艺术家ID，外键',
    `description`  TEXT COMMENT '专辑描述',
    `release_date` DATE COMMENT '专辑发布日期',
    `track_count`  INT       DEFAULT 0 COMMENT '专辑包含歌曲数量',
    `play_count`   BIGINT    DEFAULT 0 COMMENT '播放次数',
    `status`       TINYINT   DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_artist_id` (`artist_id`),
    KEY `idx_name` (`name`),
    CONSTRAINT `fk_album_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artists` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='专辑信息表';

-- 音乐分类表
DROP TABLE IF EXISTS `music_categories`;
CREATE TABLE `music_categories`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID，主键',
    `name`         VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description`  VARCHAR(500) COMMENT '分类描述',
    `parent_id`    BIGINT    DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `sort_order`   INT       DEFAULT 0 COMMENT '排序顺序',
    `status`       TINYINT   DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='音乐分类表';

-- 音乐表
DROP TABLE IF EXISTS `music`;
CREATE TABLE `music`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '音乐ID，主键',
    `title`         VARCHAR(200) NOT NULL COMMENT '歌曲名称',
    `artist_id`     BIGINT       NOT NULL COMMENT '艺术家ID，外键',
    `album_id`      BIGINT COMMENT '专辑ID，外键，可为空（单曲）',
    `album_cover`   VARCHAR(500) COMMENT '专辑封面URL',
    `duration`      INT          NOT NULL COMMENT '歌曲时长（秒）',
    `file_url`      VARCHAR(500) NOT NULL COMMENT '音频文件URL地址',
    `lrc_url`       VARCHAR(500) COMMENT '歌词文件URL地址',
    `quality`       TINYINT     DEFAULT 1 COMMENT '音质等级：1-标准，2-高品质，3-无损',
    `file_size`     BIGINT COMMENT '音频文件大小（字节）',
    `format`        VARCHAR(10) DEFAULT 'mp3' COMMENT '音频格式：mp3、flac、wav等',
    `category_id`   BIGINT COMMENT '音乐分类ID，外键',
    `play_count`    BIGINT      DEFAULT 0 COMMENT '播放次数',
    `like_count`    BIGINT      DEFAULT 0 COMMENT '点赞次数',
    `collect_count` BIGINT      DEFAULT 0 COMMENT '收藏次数',
    `status`        TINYINT     DEFAULT 1 COMMENT '状态：0-下架，1-正常',
    `release_date`  DATETIME COMMENT '发布时间',
    `created_time`  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_artist_id` (`artist_id`),
    KEY `idx_album_id` (`album_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_title` (`title`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_music_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artists` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_music_album_id` FOREIGN KEY (`album_id`) REFERENCES `albums` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_music_category_id` FOREIGN KEY (`category_id`) REFERENCES `music_categories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='音乐信息表';

-- ========================================
-- 3. 播放列表管理相关表
-- ========================================

-- 播放列表表
DROP TABLE IF EXISTS `playlists`;
CREATE TABLE `playlists`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '播放列表ID，主键',
    `name`          VARCHAR(200) NOT NULL COMMENT '播放列表名称',
    `description`   TEXT COMMENT '播放列表描述',
    `cover`         VARCHAR(500) COMMENT '播放列表封面图片URL',
    `user_id`       BIGINT       NOT NULL COMMENT '创建者用户ID，外键',
    `is_public`     TINYINT   DEFAULT 1 COMMENT '是否公开：0-私有，1-公开',
    `music_count`   INT       DEFAULT 0 COMMENT '包含歌曲数量',
    `play_count`    BIGINT    DEFAULT 0 COMMENT '播放次数',
    `collect_count` BIGINT    DEFAULT 0 COMMENT '收藏次数',
    `status`        TINYINT   DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    `created_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_name` (`name`),
    CONSTRAINT `fk_playlist_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='播放列表信息表';

-- 播放列表音乐关联表
DROP TABLE IF EXISTS `playlist_music_relations`;
CREATE TABLE `playlist_music_relations`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID，主键',
    `playlist_id` BIGINT NOT NULL COMMENT '播放列表ID，外键',
    `music_id`    BIGINT NOT NULL COMMENT '音乐ID，外键',
    `sort_order`  INT       DEFAULT 0 COMMENT '歌曲在播放列表中的排序',
    `added_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_playlist_music` (`playlist_id`, `music_id`),
    KEY `idx_playlist_id` (`playlist_id`),
    KEY `idx_music_id` (`music_id`),
    CONSTRAINT `fk_playlist_music_playlist_id` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_playlist_music_music_id` FOREIGN KEY (`music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='播放列表音乐关联表';

-- ========================================
-- 4. 用户行为统计相关表
-- ========================================

-- 用户播放历史表
DROP TABLE IF EXISTS `play_histories`;
CREATE TABLE `play_histories`
(
    `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史记录ID，主键',
    `user_id`       BIGINT NOT NULL COMMENT '用户ID，外键',
    `music_id`      BIGINT NOT NULL COMMENT '音乐ID，外键',
    `play_time`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间',
    `play_duration` INT       DEFAULT 0 COMMENT '实际播放时长（秒）',
    `device_type`   VARCHAR(50) COMMENT '设备类型',
    `ip_address`    VARCHAR(45) COMMENT '播放时的IP地址',
    `created_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_music_id` (`music_id`),
    KEY `idx_play_time` (`play_time`),
    CONSTRAINT `fk_play_history_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_play_history_music_id` FOREIGN KEY (`music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户播放历史表';

-- 用户音乐收藏表
DROP TABLE IF EXISTS `user_music_collections`;
CREATE TABLE `user_music_collections`
(
    `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID，主键',
    `user_id`      BIGINT NOT NULL COMMENT '用户ID，外键',
    `music_id`     BIGINT NOT NULL COMMENT '音乐ID，外键',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_music` (`user_id`, `music_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_music_id` (`music_id`),
    CONSTRAINT `fk_user_music_collection_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_user_music_collection_music_id` FOREIGN KEY (`music_id`) REFERENCES `music` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户音乐收藏表';