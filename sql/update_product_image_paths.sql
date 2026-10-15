-- =========================================================
-- 图片路径整理：统一 /uploads/ 前缀 + .svg 后缀
--   1) 旧前缀迁移：/images/products/ -> /uploads/products/
--   2) 后缀统一改成 .svg（.png / .jpg / 无后缀 等都会变成 .svg）
-- 可重复执行（幂等）
-- =========================================================

-- 1) 前缀迁移（商品封面 + 商品图片；库里若还是旧 /images/ 前缀时生效）
UPDATE `product`       SET `cover`     = REPLACE(`cover`, '/images/products/', '/uploads/products/');
UPDATE `product_image` SET `image_url` = REPLACE(`image_url`, '/images/products/', '/uploads/products/');

-- 2) 后缀统一为 .svg（分类图、商品封面、商品图片）
UPDATE `category`      SET `image`     = CONCAT(SUBSTRING_INDEX(`image`, '.', 1), '.svg')
    WHERE `image` IS NOT NULL AND `image` <> '';
UPDATE `product`       SET `cover`     = CONCAT(SUBSTRING_INDEX(`cover`, '.', 1), '.svg')
    WHERE `cover` IS NOT NULL AND `cover` <> '';
UPDATE `product_image` SET `image_url` = CONCAT(SUBSTRING_INDEX(`image_url`, '.', 1), '.svg')
    WHERE `image_url` IS NOT NULL AND `image_url` <> '';