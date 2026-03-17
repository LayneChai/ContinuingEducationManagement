CREATE DATABASE IF NOT EXISTS `continue_education`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `continue_education`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(255) NOT NULL COMMENT '登录密码',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `student_no` VARCHAR(50) DEFAULT NULL COMMENT '学员编号',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '教师职称/岗位',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
  KEY `idx_sys_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_sys_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号',
  `login_ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `login_location` VARCHAR(255) DEFAULT NULL COMMENT '登录地点',
  `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
  `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
  `login_status` TINYINT NOT NULL DEFAULT 1 COMMENT '登录状态 0失败 1成功',
  `message` VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_user_id` (`user_id`),
  KEY `idx_sys_login_log_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS `edu_course_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(50) DEFAULT NULL COMMENT '分类编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_course_category_code` (`code`),
  KEY `idx_edu_course_category_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类表';

CREATE TABLE IF NOT EXISTS `edu_course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_code` VARCHAR(50) DEFAULT NULL COMMENT '课程编码',
  `title` VARCHAR(200) NOT NULL COMMENT '课程标题',
  `subtitle` VARCHAR(255) DEFAULT NULL COMMENT '课程副标题',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `teacher_id` BIGINT NOT NULL COMMENT '授课教师ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `description` TEXT COMMENT '课程简介',
  `target_user` VARCHAR(255) DEFAULT NULL COMMENT '适用对象',
  `required_hours` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '要求学时',
  `total_lessons` INT NOT NULL DEFAULT 0 COMMENT '总课时数',
  `exam_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否要求考试 0否 1是',
  `assignment_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否要求作业 0否 1是',
  `certificate_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许申请证书 0否 1是',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态 0待审 1通过 2驳回',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '课程状态 0草稿 1上架 2下架',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `publish_time` DATETIME DEFAULT NULL COMMENT '上架时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_course_code` (`course_code`),
  KEY `idx_edu_course_teacher_id` (`teacher_id`),
  KEY `idx_edu_course_category_id` (`category_id`),
  KEY `idx_edu_course_status` (`status`),
  KEY `idx_edu_course_audit_status` (`audit_status`),
  CONSTRAINT `fk_edu_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_edu_course_category` FOREIGN KEY (`category_id`) REFERENCES `edu_course_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

CREATE TABLE IF NOT EXISTS `edu_course_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `title` VARCHAR(200) NOT NULL COMMENT '章节标题',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '章节说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_course_chapter_course_id` (`course_id`),
  CONSTRAINT `fk_edu_course_chapter_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程章节表';

CREATE TABLE IF NOT EXISTS `edu_course_lesson` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课时ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `chapter_id` BIGINT DEFAULT NULL COMMENT '章节ID',
  `title` VARCHAR(200) NOT NULL COMMENT '课时标题',
  `lesson_type` TINYINT NOT NULL COMMENT '课时类型 1视频 2文档 3图文',
  `resource_url` VARCHAR(255) DEFAULT NULL COMMENT '资源地址',
  `content` LONGTEXT COMMENT '图文内容',
  `duration_seconds` INT NOT NULL DEFAULT 0 COMMENT '时长(秒)',
  `preview_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许试看 0否 1是',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_course_lesson_course_id` (`course_id`),
  KEY `idx_edu_course_lesson_chapter_id` (`chapter_id`),
  CONSTRAINT `fk_edu_course_lesson_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_course_lesson_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `edu_course_chapter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程课时表';

CREATE TABLE IF NOT EXISTS `edu_course_material` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资料ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `lesson_id` BIGINT DEFAULT NULL COMMENT '关联课时ID',
  `name` VARCHAR(200) NOT NULL COMMENT '资料名称',
  `file_url` VARCHAR(255) NOT NULL COMMENT '资料地址',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_course_material_course_id` (`course_id`),
  KEY `idx_edu_course_material_lesson_id` (`lesson_id`),
  CONSTRAINT `fk_edu_course_material_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_course_material_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `edu_course_lesson` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程资料表';

CREATE TABLE IF NOT EXISTS `edu_course_enrollment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `enroll_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0已报名 1学习中 2已完成 3已取消',
  `progress_percent` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '完成进度',
  `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `source` VARCHAR(50) DEFAULT NULL COMMENT '报名来源',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_course_enrollment` (`course_id`, `student_id`),
  KEY `idx_edu_course_enrollment_student_id` (`student_id`),
  KEY `idx_edu_course_enrollment_status` (`status`),
  CONSTRAINT `fk_edu_course_enrollment_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_course_enrollment_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程报名表';

CREATE TABLE IF NOT EXISTS `edu_learning_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学习记录ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `lesson_id` BIGINT NOT NULL COMMENT '课时ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `study_seconds` INT NOT NULL DEFAULT 0 COMMENT '累计学习秒数',
  `progress_percent` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '课时完成进度',
  `last_position` INT NOT NULL DEFAULT 0 COMMENT '最后播放位置(秒)',
  `is_completed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成 0否 1是',
  `first_study_time` DATETIME DEFAULT NULL COMMENT '首次学习时间',
  `last_study_time` DATETIME DEFAULT NULL COMMENT '最后学习时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_learning_record` (`lesson_id`, `student_id`),
  KEY `idx_edu_learning_record_course_id` (`course_id`),
  KEY `idx_edu_learning_record_student_id` (`student_id`),
  CONSTRAINT `fk_edu_learning_record_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_learning_record_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `edu_course_lesson` (`id`),
  CONSTRAINT `fk_edu_learning_record_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时学习记录表';

CREATE TABLE IF NOT EXISTS `edu_course_hour_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `required_hours` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '要求学时',
  `completed_hours` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '已完成学时',
  `completion_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '完成率',
  `qualified` TINYINT NOT NULL DEFAULT 0 COMMENT '是否达标 0否 1是',
  `last_calc_time` DATETIME DEFAULT NULL COMMENT '最后统计时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_course_hour_stat` (`course_id`, `student_id`),
  KEY `idx_edu_course_hour_stat_student_id` (`student_id`),
  CONSTRAINT `fk_edu_course_hour_stat_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_course_hour_stat_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程学时统计表';

CREATE TABLE IF NOT EXISTS `edu_learning_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学习日志ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `lesson_id` BIGINT NOT NULL COMMENT '课时ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `action_type` VARCHAR(50) NOT NULL COMMENT '行为类型',
  `action_value` VARCHAR(255) DEFAULT NULL COMMENT '行为值',
  `study_seconds` INT NOT NULL DEFAULT 0 COMMENT '本次学习秒数',
  `client_ip` VARCHAR(64) DEFAULT NULL COMMENT '客户端IP',
  `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_learning_log_student_id` (`student_id`),
  KEY `idx_edu_learning_log_course_id` (`course_id`),
  CONSTRAINT `fk_edu_learning_log_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_learning_log_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `edu_course_lesson` (`id`),
  CONSTRAINT `fk_edu_learning_log_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习行为日志表';

CREATE TABLE IF NOT EXISTS `edu_exam` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考试ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `title` VARCHAR(200) NOT NULL COMMENT '考试名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '考试说明',
  `duration_minutes` INT NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
  `total_score` DECIMAL(6,2) NOT NULL DEFAULT 100.00 COMMENT '总分',
  `pass_score` DECIMAL(6,2) NOT NULL DEFAULT 60.00 COMMENT '及格分',
  `attempt_limit` INT NOT NULL DEFAULT 1 COMMENT '最大考试次数',
  `question_count` INT NOT NULL DEFAULT 0 COMMENT '题目数量',
  `start_time` DATETIME DEFAULT NULL COMMENT '开考时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0未发布 1已发布 2已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_exam_course_id` (`course_id`),
  KEY `idx_edu_exam_status` (`status`),
  CONSTRAINT `fk_edu_exam_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

CREATE TABLE IF NOT EXISTS `edu_exam_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `question_type` TINYINT NOT NULL COMMENT '题型 1单选 2多选 3判断 4简答',
  `stem` TEXT NOT NULL COMMENT '题干',
  `analysis` TEXT COMMENT '解析',
  `difficulty` TINYINT NOT NULL DEFAULT 1 COMMENT '难度 1简单 2普通 3困难',
  `score` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '默认分值',
  `correct_answer` TEXT COMMENT '正确答案',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_exam_question_course_id` (`course_id`),
  KEY `idx_edu_exam_question_type` (`question_type`),
  CONSTRAINT `fk_edu_exam_question_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试题目表';

CREATE TABLE IF NOT EXISTS `edu_exam_question_option` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '选项ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `option_label` VARCHAR(10) NOT NULL COMMENT '选项标识',
  `option_content` VARCHAR(1000) NOT NULL COMMENT '选项内容',
  `is_correct` TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确答案 0否 1是',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_edu_exam_question_option_question_id` (`question_id`),
  CONSTRAINT `fk_edu_exam_question_option_question` FOREIGN KEY (`question_id`) REFERENCES `edu_exam_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试题目选项表';

CREATE TABLE IF NOT EXISTS `edu_exam_paper_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `score` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '题目分值',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_exam_paper_question` (`exam_id`, `question_id`),
  KEY `idx_edu_exam_paper_question_question_id` (`question_id`),
  CONSTRAINT `fk_edu_exam_paper_question_exam` FOREIGN KEY (`exam_id`) REFERENCES `edu_exam` (`id`),
  CONSTRAINT `fk_edu_exam_paper_question_question` FOREIGN KEY (`question_id`) REFERENCES `edu_exam_question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试题目关联表';

CREATE TABLE IF NOT EXISTS `edu_exam_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考试记录ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `attempt_no` INT NOT NULL DEFAULT 1 COMMENT '第几次考试',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0未开始 1进行中 2已提交 3已批改',
  `objective_score` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '客观题得分',
  `subjective_score` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '主观题得分',
  `total_score` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '总得分',
  `is_pass` TINYINT NOT NULL DEFAULT 0 COMMENT '是否通过 0否 1是',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `review_time` DATETIME DEFAULT NULL COMMENT '批改时间',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '批改人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_exam_record_exam_id` (`exam_id`),
  KEY `idx_edu_exam_record_student_id` (`student_id`),
  KEY `idx_edu_exam_record_course_id` (`course_id`),
  CONSTRAINT `fk_edu_exam_record_exam` FOREIGN KEY (`exam_id`) REFERENCES `edu_exam` (`id`),
  CONSTRAINT `fk_edu_exam_record_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_exam_record_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_edu_exam_record_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试记录表';

CREATE TABLE IF NOT EXISTS `edu_exam_answer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '答题ID',
  `record_id` BIGINT NOT NULL COMMENT '考试记录ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `student_answer` TEXT COMMENT '学员答案',
  `is_correct` TINYINT DEFAULT NULL COMMENT '是否正确 0否 1是',
  `score` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '最终得分',
  `ai_score` DECIMAL(6,2) DEFAULT NULL COMMENT 'AI建议得分',
  `ai_comment` VARCHAR(1000) DEFAULT NULL COMMENT 'AI批改意见',
  `review_comment` VARCHAR(1000) DEFAULT NULL COMMENT '教师评语',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_exam_answer` (`record_id`, `question_id`),
  KEY `idx_edu_exam_answer_exam_id` (`exam_id`),
  KEY `idx_edu_exam_answer_student_id` (`student_id`),
  CONSTRAINT `fk_edu_exam_answer_record` FOREIGN KEY (`record_id`) REFERENCES `edu_exam_record` (`id`),
  CONSTRAINT `fk_edu_exam_answer_exam` FOREIGN KEY (`exam_id`) REFERENCES `edu_exam` (`id`),
  CONSTRAINT `fk_edu_exam_answer_question` FOREIGN KEY (`question_id`) REFERENCES `edu_exam_question` (`id`),
  CONSTRAINT `fk_edu_exam_answer_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试答题明细表';

CREATE TABLE IF NOT EXISTS `edu_assignment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `title` VARCHAR(200) NOT NULL COMMENT '作业标题',
  `description` TEXT COMMENT '作业说明',
  `total_score` DECIMAL(6,2) NOT NULL DEFAULT 100.00 COMMENT '总分',
  `allow_resubmit` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许重复提交 0否 1是',
  `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1已发布 2已结束',
  `create_by` BIGINT NOT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_assignment_course_id` (`course_id`),
  CONSTRAINT `fk_edu_assignment_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_assignment_create_by` FOREIGN KEY (`create_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

CREATE TABLE IF NOT EXISTS `edu_assignment_submission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `assignment_id` BIGINT NOT NULL COMMENT '作业ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `content` LONGTEXT COMMENT '提交内容',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '附件名称',
  `file_url` VARCHAR(255) DEFAULT NULL COMMENT '附件地址',
  `file_size` BIGINT DEFAULT NULL COMMENT '附件大小',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '附件类型',
  `submit_count` INT NOT NULL DEFAULT 1 COMMENT '提交次数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0未提交 1已提交 2已批改',
  `submitted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_edu_assignment_submission_assignment_id` (`assignment_id`),
  KEY `idx_edu_assignment_submission_student_id` (`student_id`),
  CONSTRAINT `fk_edu_assignment_submission_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `edu_assignment` (`id`),
  CONSTRAINT `fk_edu_assignment_submission_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_assignment_submission_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交表';

CREATE TABLE IF NOT EXISTS `edu_assignment_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '批改ID',
  `submission_id` BIGINT NOT NULL COMMENT '提交ID',
  `reviewer_id` BIGINT NOT NULL COMMENT '批改教师ID',
  `score` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '评分',
  `comment` VARCHAR(1000) DEFAULT NULL COMMENT '评语',
  `ai_comment` VARCHAR(1000) DEFAULT NULL COMMENT 'AI建议评语',
  `review_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '批改时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_assignment_review_submission_id` (`submission_id`),
  KEY `idx_edu_assignment_review_reviewer_id` (`reviewer_id`),
  CONSTRAINT `fk_edu_assignment_review_submission` FOREIGN KEY (`submission_id`) REFERENCES `edu_assignment_submission` (`id`),
  CONSTRAINT `fk_edu_assignment_review_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业批改表';

CREATE TABLE IF NOT EXISTS `edu_certificate_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `background_url` VARCHAR(255) DEFAULT NULL COMMENT '背景图地址',
  `seal_url` VARCHAR(255) DEFAULT NULL COMMENT '签章图地址',
  `content_config` JSON DEFAULT NULL COMMENT '文案配置',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书模板表';

CREATE TABLE IF NOT EXISTS `edu_certificate_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `student_id` BIGINT NOT NULL COMMENT '学员ID',
  `template_id` BIGINT DEFAULT NULL COMMENT '模板ID',
  `apply_reason` VARCHAR(500) DEFAULT NULL COMMENT '申请说明',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1通过 2驳回',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_certificate_apply` (`course_id`, `student_id`),
  KEY `idx_edu_certificate_apply_status` (`status`),
  CONSTRAINT `fk_edu_certificate_apply_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`),
  CONSTRAINT `fk_edu_certificate_apply_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_edu_certificate_apply_template` FOREIGN KEY (`template_id`) REFERENCES `edu_certificate_template` (`id`),
  CONSTRAINT `fk_edu_certificate_apply_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书申请表';

CREATE TABLE IF NOT EXISTS `edu_certificate_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证书记录ID',
  `apply_id` BIGINT NOT NULL COMMENT '申请ID',
  `certificate_no` VARCHAR(100) NOT NULL COMMENT '证书编号',
  `certificate_title` VARCHAR(200) NOT NULL COMMENT '证书标题',
  `certificate_url` VARCHAR(255) DEFAULT NULL COMMENT '证书文件地址',
  `issue_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '颁发时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0作废 1有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_certificate_record_apply_id` (`apply_id`),
  UNIQUE KEY `uk_edu_certificate_record_certificate_no` (`certificate_no`),
  CONSTRAINT `fk_edu_certificate_record_apply` FOREIGN KEY (`apply_id`) REFERENCES `edu_certificate_apply` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书记录表';

CREATE TABLE IF NOT EXISTS `sys_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` LONGTEXT NOT NULL COMMENT '公告内容',
  `target_type` TINYINT NOT NULL DEFAULT 0 COMMENT '发送范围 0全员 1角色 2用户 3课程',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0草稿 1已发布 2已撤回',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_notice_status` (`status`),
  CONSTRAINT `fk_sys_notice_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

CREATE TABLE IF NOT EXISTS `sys_notice_target` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `notice_id` BIGINT NOT NULL COMMENT '公告ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型 1角色 2用户 3课程',
  `target_id` BIGINT NOT NULL COMMENT '目标对象ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_notice_target` (`notice_id`, `target_type`, `target_id`),
  KEY `idx_sys_notice_target_target_id` (`target_id`),
  CONSTRAINT `fk_sys_notice_target_notice` FOREIGN KEY (`notice_id`) REFERENCES `sys_notice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告推送目标表';

CREATE TABLE IF NOT EXISTS `sys_notice_read` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '已读ID',
  `notice_id` BIGINT NOT NULL COMMENT '公告ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_notice_read` (`notice_id`, `user_id`),
  KEY `idx_sys_notice_read_user_id` (`user_id`),
  CONSTRAINT `fk_sys_notice_read_notice` FOREIGN KEY (`notice_id`) REFERENCES `sys_notice` (`id`),
  CONSTRAINT `fk_sys_notice_read_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告已读表';

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `course_id` BIGINT DEFAULT NULL COMMENT '课程ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0关闭 1进行中',
  `message_count` INT NOT NULL DEFAULT 0 COMMENT '消息数',
  `total_tokens` INT NOT NULL DEFAULT 0 COMMENT '累计Token',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_session_user_id` (`user_id`),
  KEY `idx_ai_chat_session_course_id` (`course_id`),
  CONSTRAINT `fk_ai_chat_session_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_ai_chat_session_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `course_id` BIGINT DEFAULT NULL COMMENT '课程ID',
  `message_role` VARCHAR(20) NOT NULL COMMENT '角色 user assistant system',
  `content` LONGTEXT NOT NULL COMMENT '消息内容',
  `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
  `token_count` INT NOT NULL DEFAULT 0 COMMENT 'Token消耗',
  `response_time_ms` INT DEFAULT NULL COMMENT '响应耗时',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_message_session_id` (`session_id`),
  KEY `idx_ai_chat_message_user_id` (`user_id`),
  CONSTRAINT `fk_ai_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session` (`id`),
  CONSTRAINT `fk_ai_chat_message_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_ai_chat_message_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

CREATE TABLE IF NOT EXISTS `ai_chat_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计ID',
  `message_id` BIGINT NOT NULL COMMENT '消息ID',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态 0通过 1拦截 2人工复核',
  `risk_type` VARCHAR(100) DEFAULT NULL COMMENT '风险类型',
  `risk_detail` VARCHAR(1000) DEFAULT NULL COMMENT '风险详情',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_audit_message_id` (`message_id`),
  CONSTRAINT `fk_ai_chat_audit_message` FOREIGN KEY (`message_id`) REFERENCES `ai_chat_message` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息审计表';

CREATE TABLE IF NOT EXISTS `ai_model_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型配置ID',
  `provider_name` VARCHAR(100) NOT NULL COMMENT '服务商名称',
  `display_name` VARCHAR(100) NOT NULL COMMENT '显示名称',
  `base_url` VARCHAR(255) NOT NULL COMMENT '接口基础地址',
  `api_key` VARCHAR(255) NOT NULL COMMENT '模型密钥',
  `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
  `enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用 0否 1是',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_model_config_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';

INSERT INTO `sys_role` (`role_code`, `role_name`, `status`, `sort`, `remark`)
VALUES
  ('ADMIN', '管理员', 1, 1, '系统管理员角色'),
  ('TEACHER', '教师', 1, 2, '授课教师角色'),
  ('STUDENT', '学生', 1, 3, '继续教育学员角色')
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `status` = VALUES(`status`),
  `sort` = VALUES(`sort`),
  `remark` = VALUES(`remark`);

SET FOREIGN_KEY_CHECKS = 1;
