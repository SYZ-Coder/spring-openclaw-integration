// 声明配置类所在包。
package com.example.openclaw.config;

// 导入 Bean 注解。
import org.springframework.context.annotation.Bean;
// 导入配置类注解。
import org.springframework.context.annotation.Configuration;
// 导入 HTTP 头常量。
import org.springframework.http.HttpHeaders;
// 导入 WebClient。
import org.springframework.web.reactive.function.client.WebClient;

// 标记当前类是 Spring 配置类。
@Configuration
public class OpenClawClientConfig {

    // 注册一个专门调用 OpenClaw 的 WebClient Bean。
    @Bean
    public WebClient openClawWebClient(OpenClawProperties properties) {
        // 构建 WebClient，并设置基础地址与默认鉴权头。
        return WebClient.builder()
                // 设置 OpenClaw Gateway 基础地址。
                .baseUrl(properties.getBaseUrl())
                // 设置默认 Authorization 头，避免重复拼接 Token。
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken())
                // 构建最终客户端实例。
                .build();
    }
}
