package com.qizhi.continueeducation.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI continueEducationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("启智继续教育管理系统 API")
                        .description("继续教育场景的在线教育管理平台后端接口文档")
                        .version("v0.0.1")
                        .contact(new Contact().name("QiZhi")))
                .externalDocs(new ExternalDocumentation()
                        .description("Knife4j Docs")
                        .url("/doc.html"));
    }
}
