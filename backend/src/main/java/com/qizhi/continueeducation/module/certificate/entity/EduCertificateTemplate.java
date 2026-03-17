package com.qizhi.continueeducation.module.certificate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("edu_certificate_template")
public class EduCertificateTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    private String backgroundUrl;
    private String sealUrl;
    private String contentConfig;
    private Integer status;
}
