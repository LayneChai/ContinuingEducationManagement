package com.qizhi.continueeducation.module.ai.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekChatMessage {
    private String role;
    private String content;
}
