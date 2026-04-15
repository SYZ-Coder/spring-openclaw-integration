// 声明当前主启动类所在的包路径。
package com.example.openclaw;

// 导入自定义配置属性类，用于读取 application.yml 中的 openclaw 配置。
import com.example.openclaw.config.OpenClawProperties;
// 导入 Spring Boot 启动器。
import org.springframework.boot.SpringApplication;
// 导入 Spring Boot 主启动注解。
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 导入配置属性启用注解。
import org.springframework.boot.context.properties.EnableConfigurationProperties;

// 标记这是一个 Spring Boot 应用主类。
@SpringBootApplication
// 显式启用 OpenClawProperties 的属性绑定能力。
@EnableConfigurationProperties(OpenClawProperties.class)
public class OpenClawSpringApplication {

    // Java 应用入口方法。
    public static void main(String[] args) {
        // 启动整个 Spring Boot 应用上下文。
        SpringApplication.run(OpenClawSpringApplication.class, args);
    }
}
