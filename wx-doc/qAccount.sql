/*
 Navicat Premium Dump SQL

 Source Server         : tencent_mysql8
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36-0ubuntu0.20.04.1)
 Source Host           : 118.89.57.96:3306
 Source Schema         : qAccount

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36-0ubuntu0.20.04.1)
 File Encoding         : 65001

 Date: 15/05/2025 16:12:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sign_id` bigint NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `title_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `content_imgs` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `draft_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_del` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for meal
-- ----------------------------
DROP TABLE IF EXISTS `meal`;
CREATE TABLE `meal`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sign_id` bigint NULL DEFAULT NULL COMMENT 'sign_id',
  `time_now` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '今天的日期(2025-05-09)',
  `breakfast` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '早餐',
  `lunch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '午餐',
  `fruit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '早点',
  `lunch_middle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下午茶',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `orgin_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '原始数据',
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_del` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sign_id`(`sign_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 234 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sign
-- ----------------------------
DROP TABLE IF EXISTS `sign`;
CREATE TABLE `sign`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `time_now` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '今天的日期(2025-05-09)',
  `sign_in_time` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '签到时间',
  `sign_out_time` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '签退时间',
  `sign_in_pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '签到照片(只记录一张)',
  `sign_out_pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '签退照片(只记录一张)',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NULL DEFAULT 0 COMMENT '已删除，0为否，1为是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_time_now`(`time_now` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 283 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sign_picture
-- ----------------------------
DROP TABLE IF EXISTS `sign_picture`;
CREATE TABLE `sign_picture`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sign_id` bigint NULL DEFAULT NULL,
  `sign_picture` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '签到或签退照片',
  `type` tinyint NULL DEFAULT 2 COMMENT '1为签到，2为签退；默认为2',
  `spider_status` tinyint NULL DEFAULT 0 COMMENT '0未爬取，1已爬取，2爬取异常',
  `sign_picture_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '签到或签退原始照片',
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_del` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sign_id`(`sign_id` ASC) USING BTREE,
  INDEX `idx_spider_status`(`spider_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 187 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '签到照片表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
