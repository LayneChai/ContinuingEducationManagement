package com.qizhi.continueeducation.module.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("edu_certificate_apply")
public class EduCertificateApply {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long studentId;
    private Long templateId;
    private String applyReason;
    private Integer status;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
}
