package com.qizhi.continueeducation.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qizhi.continueeducation.module.course.entity.EduCourseCategory;
import com.qizhi.continueeducation.module.course.mapper.EduCourseCategoryMapper;
import com.qizhi.continueeducation.module.system.entity.SysRole;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysRoleMapper;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import com.qizhi.continueeducation.module.system.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemBootstrapService implements CommandLineRunner {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final EduCourseCategoryMapper courseCategoryMapper;
    private final UserRoleService userRoleService;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureStudentClassColumn();
        ensureAiChatMessageColumns();
        long roleCount = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>());
        initDefaultAdmin();
        initDefaultCourseCategories();
        log.info("System bootstrap finished, role count: {}", roleCount);
    }

    private void ensureStudentClassColumn() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'class_name'",
                Integer.class
        );
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN class_name VARCHAR(100) DEFAULT NULL COMMENT '班级名称' AFTER student_no");
        log.info("Added sys_user.class_name column");
    }

    private void ensureAiChatMessageColumns() {
        if (!hasColumn("ai_chat_message", "update_time")) {
            jdbcTemplate.execute("ALTER TABLE ai_chat_message ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time");
            log.info("Added ai_chat_message.update_time column");
        }
        if (!hasColumn("ai_chat_message", "response_time_ms")) {
            jdbcTemplate.execute("ALTER TABLE ai_chat_message ADD COLUMN response_time_ms INT DEFAULT NULL COMMENT '响应耗时' AFTER token_count");
            log.info("Added ai_chat_message.response_time_ms column");
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private void initDefaultAdmin() {
        SysUser admin = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin")
                .eq(SysUser::getDeleted, 0)
                .last("limit 1"));
        if (admin != null) {
            return;
        }

        SysUser user = new SysUser();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("Admin@123456"));
        user.setRealName("系统管理员");
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);
        userRoleService.bindRoles(user.getId(), Collections.singletonList("ADMIN"));
        log.info("Initialized default admin account: username=admin, password=Admin@123456");
    }

    private void initDefaultCourseCategories() {
        long categoryCount = courseCategoryMapper.selectCount(new LambdaQueryWrapper<EduCourseCategory>());
        if (categoryCount > 0) {
            return;
        }

        List<String> categoryNames = Arrays.asList(
                "公需课",
                "专业技术",
                "职业技能",
                "岗位培训",
                "政策法规",
                "信息技术",
                "安全生产",
                "管理能力",
                "学历提升",
                "素质教育",
                "其他"
        );

        for (int i = 0; i < categoryNames.size(); i++) {
            EduCourseCategory category = new EduCourseCategory();
            category.setParentId(0L);
            category.setName(categoryNames.get(i));
            category.setCode("DEFAULT_" + (i + 1));
            category.setSort(i + 1);
            category.setStatus(1);
            category.setRemark("系统默认课程分类");
            courseCategoryMapper.insert(category);
        }

        log.info("Initialized default course categories: {}", categoryNames.size());
    }
}
