package com.qizhi.continueeducation.module.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qizhi.continueeducation.module.ai.entity.AiChatSession;
import com.qizhi.continueeducation.module.ai.mapper.AiChatSessionMapper;
import com.qizhi.continueeducation.module.certificate.entity.EduCertificateApply;
import com.qizhi.continueeducation.module.certificate.entity.EduCertificateRecord;
import com.qizhi.continueeducation.module.certificate.mapper.EduCertificateApplyMapper;
import com.qizhi.continueeducation.module.certificate.mapper.EduCertificateRecordMapper;
import com.qizhi.continueeducation.module.course.entity.EduCourse;
import com.qizhi.continueeducation.module.course.entity.EduCourseEnrollment;
import com.qizhi.continueeducation.module.course.mapper.EduCourseEnrollmentMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseMapper;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import com.qizhi.continueeducation.module.system.service.PortalService;
import com.qizhi.continueeducation.module.system.service.UserRoleService;
import com.qizhi.continueeducation.module.system.vo.DashboardCardVo;
import com.qizhi.continueeducation.module.system.vo.DashboardVo;
import com.qizhi.continueeducation.module.system.vo.MenuItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortalServiceImpl implements PortalService {

    private final UserRoleService userRoleService;
    private final SysUserMapper sysUserMapper;
    private final EduCourseMapper eduCourseMapper;
    private final EduCourseEnrollmentMapper enrollmentMapper;
    private final EduCertificateApplyMapper certificateApplyMapper;
    private final EduCertificateRecordMapper certificateRecordMapper;
    private final AiChatSessionMapper aiChatSessionMapper;

    @Override
    public List<MenuItemVo> getCurrentUserMenus() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roleCodes = userRoleService.getRoleCodesByUserId(userId);
        List<MenuItemVo> menus = new ArrayList<>();

        if (roleCodes.contains(RoleCode.ADMIN)) {
            menus.add(buildAdminMenu());
        }
        if (roleCodes.contains(RoleCode.TEACHER)) {
            menus.add(buildTeacherMenu());
        }
        if (roleCodes.contains(RoleCode.STUDENT)) {
            menus.add(buildStudentMenu());
        }
        return menus;
    }

    @Override
    public DashboardVo getCurrentUserDashboard() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = userRoleService.getRoleCodesByUserId(userId);
        String primaryRole = resolvePrimaryRole(roles);
        return switch (primaryRole) {
            case RoleCode.ADMIN -> buildAdminDashboard();
            case RoleCode.TEACHER -> buildTeacherDashboard(userId);
            default -> buildStudentDashboard(userId);
        };
    }

    private String resolvePrimaryRole(List<String> roles) {
        if (roles.contains(RoleCode.ADMIN)) {
            return RoleCode.ADMIN;
        }
        if (roles.contains(RoleCode.TEACHER)) {
            return RoleCode.TEACHER;
        }
        return RoleCode.STUDENT;
    }

    private DashboardVo buildAdminDashboard() {
        long totalUsers = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeleted, 0));
        long totalTeachers = countUsersByRole(RoleCode.TEACHER);
        long totalStudents = countUsersByRole(RoleCode.STUDENT);
        long totalCourses = eduCourseMapper.selectCount(new LambdaQueryWrapper<>());
        long publishedCourses = eduCourseMapper.selectCount(new LambdaQueryWrapper<EduCourse>().eq(EduCourse::getStatus, 1));
        long aiSessions = aiChatSessionMapper.selectCount(new LambdaQueryWrapper<AiChatSession>());

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("pendingCourseAudit", eduCourseMapper.selectCount(new LambdaQueryWrapper<EduCourse>().eq(EduCourse::getAuditStatus, 0)));
        extra.put("publishedCourseRate", totalCourses == 0 ? 0 : publishedCourses * 100 / totalCourses);

        return DashboardVo.builder()
                .roleCode(RoleCode.ADMIN)
                .title("管理员工作台")
                .cards(List.of(
                        card("users", "平台用户", totalUsers),
                        card("teachers", "教师数量", totalTeachers),
                        card("students", "学员数量", totalStudents),
                        card("courses", "课程总数", totalCourses),
                        card("publishedCourses", "已上架课程", publishedCourses),
                        card("aiSessions", "AI会话数", aiSessions)
                ))
                .extra(extra)
                .build();
    }

    private DashboardVo buildTeacherDashboard(Long userId) {
        long totalCourses = eduCourseMapper.selectCount(new LambdaQueryWrapper<EduCourse>().eq(EduCourse::getTeacherId, userId));
        long publishedCourses = eduCourseMapper.selectCount(new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getTeacherId, userId)
                .eq(EduCourse::getStatus, 1));
        List<EduCourse> courses = eduCourseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                .select(EduCourse::getId)
                .eq(EduCourse::getTeacherId, userId));
        List<Long> courseIds = courses.stream().map(EduCourse::getId).toList();
        long enrollments = courseIds.isEmpty() ? 0 : enrollmentMapper.selectCount(new LambdaQueryWrapper<EduCourseEnrollment>().in(EduCourseEnrollment::getCourseId, courseIds));
        long aiSessions = courseIds.isEmpty() ? 0 : aiChatSessionMapper.selectCount(new LambdaQueryWrapper<AiChatSession>().in(AiChatSession::getCourseId, courseIds));

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("draftCourses", eduCourseMapper.selectCount(new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getTeacherId, userId)
                .eq(EduCourse::getStatus, 0)));
        extra.put("teachingStudents", enrollments);

        return DashboardVo.builder()
                .roleCode(RoleCode.TEACHER)
                .title("教师工作台")
                .cards(List.of(
                        card("myCourses", "我的课程", totalCourses),
                        card("publishedCourses", "已发布课程", publishedCourses),
                        card("studentEnrollments", "选课人次", enrollments),
                        card("aiSessions", "AI辅导会话", aiSessions)
                ))
                .extra(extra)
                .build();
    }

    private DashboardVo buildStudentDashboard(Long userId) {
        long enrolledCourses = enrollmentMapper.selectCount(new LambdaQueryWrapper<EduCourseEnrollment>().eq(EduCourseEnrollment::getStudentId, userId));
        long learningCourses = enrollmentMapper.selectCount(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getStudentId, userId)
                .eq(EduCourseEnrollment::getStatus, 1));
        long completedCourses = enrollmentMapper.selectCount(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getStudentId, userId)
                .eq(EduCourseEnrollment::getStatus, 2));
        List<Long> applyIds = certificateApplyMapper.selectList(new LambdaQueryWrapper<EduCertificateApply>()
                        .select(EduCertificateApply::getId)
                        .eq(EduCertificateApply::getStudentId, userId)
                        .eq(EduCertificateApply::getStatus, 1))
                .stream()
                .map(EduCertificateApply::getId)
                .toList();
        long certificates = applyIds.isEmpty() ? 0 : certificateRecordMapper.selectCount(new LambdaQueryWrapper<EduCertificateRecord>()
                .in(EduCertificateRecord::getApplyId, applyIds)
                .eq(EduCertificateRecord::getStatus, 1));
        long aiSessions = aiChatSessionMapper.selectCount(new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getUserId, userId));

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("completionRate", enrolledCourses == 0 ? 0 : completedCourses * 100 / enrolledCourses);
        extra.put("activeLearningCourses", learningCourses);

        return DashboardVo.builder()
                .roleCode(RoleCode.STUDENT)
                .title("学员学习台")
                .cards(List.of(
                        card("enrolledCourses", "已报名课程", enrolledCourses),
                        card("learningCourses", "学习中课程", learningCourses),
                        card("completedCourses", "已完成课程", completedCourses),
                        card("certificates", "已获证书", certificates),
                        card("aiSessions", "AI提问会话", aiSessions)
                ))
                .extra(extra)
                .build();
    }

    private long countUsersByRole(String roleCode) {
        return userRoleService.getUserIdsByRoleCode(roleCode).size();
    }

    private DashboardCardVo card(String key, String title, long value) {
        return DashboardCardVo.builder()
                .key(key)
                .title(title)
                .value(value)
                .unit("个")
                .build();
    }

    private MenuItemVo buildAdminMenu() {
        return MenuItemVo.builder()
                .name("admin")
                .path("/admin")
                .component("Layout")
                .title("管理中心")
                .icon("Setting")
                .permission("portal:view")
                .children(List.of(
                        menu("AdminDashboard", "/admin/dashboard", "admin/dashboard/index", "数据看板", "Odometer", "dashboard:admin"),
                        menu("AdminUsers", "/admin/users", "admin/user/index", "学员管理", "User", "student:manage"),
                        menu("AdminTeachers", "/admin/teachers", "admin/teacher/index", "教师管理", "Avatar", "teacher:manage"),
                        menu("AdminCourseAudit", "/admin/course-audit", "admin/course-audit/index", "课程审核", "Tickets", "course:audit"),
                        menu("AdminCertificates", "/admin/certificates", "admin/certificate/index", "证书审核", "Postcard", "certificate:audit"),
                        menu("AdminAiModels", "/admin/ai-models", "admin/ai-model/index", "模型配置", "Cpu", "portal:view")
                ))
                .build();
    }

    private MenuItemVo buildTeacherMenu() {
        return MenuItemVo.builder()
                .name("teacher")
                .path("/teacher")
                .component("Layout")
                .title("教师中心")
                .icon("Reading")
                .permission("portal:view")
                .children(List.of(
                        menu("TeacherDashboard", "/teacher/dashboard", "teacher/dashboard/index", "工作台", "DataLine", "dashboard:teacher"),
                        menu("TeacherCourses", "/teacher/courses", "teacher/course/index", "课程管理", "Notebook", "course:manage"),
                        menu("TeacherExams", "/teacher/exams", "teacher/exam/index", "考试管理", "EditPen", "exam:manage"),
                        menu("TeacherAssignments", "/teacher/assignments", "teacher/assignment/index", "作业管理", "DocumentChecked", "assignment:manage"),
                        menu("TeacherAiLogs", "/teacher/ai-logs", "teacher/ai/index", "AI记录", "ChatDotRound", "ai:review")
                ))
                .build();
    }

    private MenuItemVo buildStudentMenu() {
        return MenuItemVo.builder()
                .name("student")
                .path("/student")
                .component("Layout")
                .title("学习中心")
                .icon("School")
                .permission("portal:view")
                .children(List.of(
                        menu("StudentDashboard", "/student/dashboard", "student/dashboard/index", "学习台", "House", "dashboard:student"),
                        menu("StudentCourses", "/student/courses", "student/course/index", "课程学习", "Reading", "course:learn"),
                        menu("StudentExams", "/student/exams", "student/exam/index", "在线考试", "Memo", "exam:take"),
                        menu("StudentAssignments", "/student/assignments", "student/assignment/index", "作业提交", "Files", "assignment:submit"),
                        menu("StudentCertificates", "/student/certificates", "student/certificate/index", "证书中心", "Medal", "certificate:apply"),
                        menu("StudentAi", "/student/ai", "student/ai/index", "AI辅导", "Promotion", "ai:chat")
                ))
                .build();
    }

    private MenuItemVo menu(String name, String path, String component, String title, String icon, String permission) {
        return MenuItemVo.builder()
                .name(name)
                .path(path)
                .component(component)
                .title(title)
                .icon(icon)
                .permission(permission)
                .children(List.of())
                .build();
    }
}
