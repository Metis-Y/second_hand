-- =========================================================
-- 商品图片路径迁移：把商品封面 / 商品图片从旧前缀迁到 /uploads/products/
-- 旧的演示数据路径是 /images/products/xxx，现统一改为 /uploads/products/xxx
-- 可重复执行（幂等）
-- =========================================================

UPDATE `product`
SET `cover` = REPLACE(`cover`, '/images/products/', '/uploads/products/');

UPDATE `product_image`
SET `image_url` = REPLACE(`image_url`, '/images/products/', '/uploads/products/');