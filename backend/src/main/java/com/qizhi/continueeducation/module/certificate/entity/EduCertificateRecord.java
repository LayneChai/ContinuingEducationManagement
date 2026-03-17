package com.qizhi.continueeducation.module.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("edu_certificate_record")
public class EduCertificateRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applyId;
    private String certificateNo;
    private String certificateTitle;
    private String certificateUrl;
    private LocalDateTime issueTime;
    private Integer status;
}
