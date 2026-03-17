package com.qizhi.continueeducation.module.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LessonStudyRequest {

    @NotNull(message = "学习秒数不能为空")
    @Min(value = 0, message = "学习秒数不能小于0")
    private Integer studySeconds;

    @NotNull(message = "进度不能为空")
    private BigDecimal progressPercent;

    @NotNull(message = "播放位置不能为空")
    @Min(value = 0, message = "播放位置不能小于0")
    private Integer lastPosition;

    private Integer completed;
}
