-- 为真实访问模式增加组合索引

-- 我的文章：支持按 user_id 过滤并按创建时间倒序分页。
CREATE INDEX `idx_user_create`
    ON `article` (`user_id`, `create_time`);

-- 我的草稿/已发布文章：支持 user_id + status 过滤并按创建时间排序。
CREATE INDEX `idx_user_status_create`
    ON `article` (`user_id`, `status`, `create_time`);

-- 旧的单列 user_id 索引已被上述两个索引的最左前缀能力覆盖。
DROP INDEX `idx_user_id` ON `article`;

-- 评论顶级列表：按文章、父评论和创建时间筛选/排序。
CREATE INDEX `idx_article_parent_create`
    ON `comment` (`article_id`, `parent_id`, `create_time`);

-- 新组合索引覆盖 article_id 的最左前缀查询。
DROP INDEX `idx_article_id` ON `comment`;

-- 我的收藏：按用户过滤并按收藏时间倒序分页。
CREATE INDEX `idx_user_create`
    ON `favorite` (`user_id`, `create_time`);
