-- ========================================
-- 音乐播放器后端服务初始化数据脚本
-- 版本: 1.0.0
-- 创建时间: 2025-09-01
-- ========================================

USE `music_server`;

-- ========================================
-- 1. 初始化音乐分类数据
-- ========================================

INSERT INTO `music_categories` (`name`, `description`, `parent_id`, `sort_order`, `status`) VALUES
('流行', '流行音乐类型', 0, 1, 1),
('摇滚', '摇滚音乐类型', 0, 2, 1),
('电子', '电子音乐类型', 0, 3, 1),
('民谣', '民谣音乐类型', 0, 4, 1),
('古典', '古典音乐类型', 0, 5, 1),
('爵士', '爵士音乐类型', 0, 6, 1),
('嘻哈', '嘻哈音乐类型', 0, 7, 1),
('乡村', '乡村音乐类型', 0, 8, 1),
('中国流行', '华语流行音乐', 1, 1, 1),
('欧美流行', '欧美流行音乐', 1, 2, 1),
('日韩流行', '日韩流行音乐', 1, 3, 1);

-- ========================================
-- 2. 初始化管理员用户
-- ========================================

INSERT INTO `users` (`username`, `email`, `password`, `nickname`, `level`, `status`) VALUES
('admin', 'admin@musicserver.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXgzDOcvqqno2kfvHosdEIWOkha', '系统管理员', 99, 1);

-- 为管理员创建用户配置
INSERT INTO `user_profiles` (`user_id`, `auto_play`, `quality_preference`, `playback_mode`, `volume`, `privacy_level`) 
SELECT `id`, 1, 3, 1, 80, 1 FROM `users` WHERE `username` = 'admin';

-- ========================================
-- 3. 初始化示例艺术家数据
-- ========================================

INSERT INTO `artists` (`name`, `description`, `country`, `genre`, `status`) VALUES
('周杰伦', '华语流行音乐天王', '中国台湾', '流行', 1),
('邓紫棋', '华语流行女歌手', '中国香港', '流行', 1),
('陈奕迅', '华语流行歌手', '中国香港', '流行', 1),
('Taylor Swift', '美国流行音乐巨星', '美国', '流行', 1),
('Ed Sheeran', '英国创作型歌手', '英国', '流行', 1);

-- ========================================
-- 4. 初始化示例专辑数据
-- ========================================

INSERT INTO `albums` (`name`, `artist_id`, `description`, `release_date`, `track_count`, `status`) VALUES
('Jay', 1, '周杰伦首张个人专辑', '2000-11-07', 10, 1),
('范特西', 1, '周杰伦第二张专辑', '2001-09-20', 10, 1),
('新的心跳', 2, '邓紫棋专辑', '2015-11-06', 12, 1),
('1989', 4, 'Taylor Swift专辑', '2014-10-27', 13, 1),
('÷ (Divide)', 5, 'Ed Sheeran专辑', '2017-03-03', 16, 1);

-- ========================================
-- 5. 初始化示例音乐数据
-- ========================================

INSERT INTO `music` (`title`, `artist_id`, `album_id`, `duration`, `file_url`, `quality`, `format`, `category_id`, `status`, `release_date`) VALUES
('可爱女人', 1, 1, 221, '/music/files/keai_nvren.mp3', 1, 'mp3', 9, 1, '2000-11-07 00:00:00'),
('星晴', 1, 1, 246, '/music/files/xing_qing.mp3', 1, 'mp3', 9, 1, '2000-11-07 00:00:00'),
('爱在西元前', 1, 2, 235, '/music/files/ai_zai_xiyuanqian.mp3', 1, 'mp3', 9, 1, '2001-09-20 00:00:00'),
('夜曲', 1, NULL, 238, '/music/files/ye_qu.mp3', 1, 'mp3', 9, 1, '2005-11-01 00:00:00'),
('泡沫', 2, 3, 245, '/music/files/pao_mo.mp3', 1, 'mp3', 9, 1, '2012-07-06 00:00:00'),
('Shake It Off', 4, 4, 219, '/music/files/shake_it_off.mp3', 1, 'mp3', 10, 1, '2014-08-18 00:00:00'),
('Shape of You', 5, 5, 233, '/music/files/shape_of_you.mp3', 1, 'mp3', 10, 1, '2017-01-06 00:00:00');

-- ========================================
-- 6. 创建默认播放列表
-- ========================================

-- 为管理员创建默认播放列表
INSERT INTO `playlists` (`name`, `description`, `user_id`, `is_public`, `status`) 
SELECT '我喜欢的音乐', '默认收藏播放列表', `id`, 1, 1 FROM `users` WHERE `username` = 'admin';

INSERT INTO `playlists` (`name`, `description`, `user_id`, `is_public`, `status`) 
SELECT '华语经典', '华语经典歌曲合集', `id`, 1, 1 FROM `users` WHERE `username` = 'admin';

-- 向播放列表添加音乐
INSERT INTO `playlist_music_relations` (`playlist_id`, `music_id`, `sort_order`) VALUES
(2, 1, 1),
(2, 2, 2),
(2, 3, 3),
(2, 4, 4),
(2, 5, 5);

-- 更新播放列表音乐数量
UPDATE `playlists` SET `music_count` = 5 WHERE `id` = 2;

-- ========================================
-- 7. 创建索引优化
-- ========================================

-- 为经常查询的字段创建复合索引
CREATE INDEX `idx_music_artist_status` ON `music` (`artist_id`, `status`);
CREATE INDEX `idx_music_category_status` ON `music` (`category_id`, `status`);
CREATE INDEX `idx_play_history_user_time` ON `play_histories` (`user_id`, `play_time`);
CREATE INDEX `idx_user_music_collection_time` ON `user_music_collections` (`user_id`, `created_time`);

-- ========================================
-- 8. 数据库函数和存储过程
-- ========================================

-- 创建更新音乐播放统计的存储过程
DELIMITER $$
CREATE PROCEDURE UpdateMusicPlayCount(IN musicId BIGINT, IN userId BIGINT, IN playDuration INT)
BEGIN
    -- 更新音乐播放次数
    UPDATE `music` SET `play_count` = `play_count` + 1 WHERE `id` = musicId;
    
    -- 记录播放历史
    INSERT INTO `play_histories` (`user_id`, `music_id`, `play_duration`, `device_type`) 
    VALUES (userId, musicId, playDuration, 'web');
    
    -- 更新用户经验值（每播放一首歌曲+10经验）
    UPDATE `users` SET `experience` = `experience` + 10 WHERE `id` = userId;
END$$
DELIMITER ;

-- 创建获取热门音乐的视图
CREATE VIEW `view_hot_music` AS
SELECT 
    m.id,
    m.title,
    a.name AS artist_name,
    al.name AS album_name,
    m.duration,
    m.play_count,
    m.like_count,
    m.collect_count,
    m.status,
    m.created_time
FROM `music` m
LEFT JOIN `artists` a ON m.artist_id = a.id
LEFT JOIN `albums` al ON m.album_id = al.id
WHERE m.status = 1
ORDER BY m.play_count DESC, m.created_time DESC;

-- ========================================
-- 初始化完成提示
-- ========================================

SELECT 
    'Database initialization completed successfully!' AS message,
    NOW() AS completion_time,
    DATABASE() AS database_name;