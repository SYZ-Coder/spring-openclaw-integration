// 声明配置类所在包。
package com.example.openclaw.config;

// 导入 Spring Boot 配置属性绑定注解。
import org.springframework.boot.context.properties.ConfigurationProperties;

// 将 `openclaw.*` 配置项绑定到当前类。
@ConfigurationProperties(prefix = "openclaw")
public class OpenClawProperties {

    // 保存 OpenClaw Gateway 的基础地址。
    private String baseUrl;
    // 保存访问 Gateway `/v1/*` 接口使用的 Token。
    private String token;
    // 保存接收 OpenClaw callback 时校验的 Token。
    private String callbackToken;
    // 保存 Webhooks 插件 route 路径。
    private String taskflowWebhookPath;
    // 保存调用 Webhooks 插件 route 时使用的共享密钥。
    private String taskflowWebhookSecret;

    // 获取基础地址。
    public String getBaseUrl() {
        // 返回当前保存的基础地址。
        return baseUrl;
    }

    // 设置基础地址。
    public void setBaseUrl(String baseUrl) {
        // 将传入值写入 baseUrl 字段。
        this.baseUrl = baseUrl;
    }

    // 获取 Gateway Token。
    public String getToken() {
        // 返回当前保存的 Gateway Token。
        return token;
    }

    // 设置 Gateway Token。
    public void setToken(String token) {
        // 将传入值写入 token 字段。
        this.token = token;
    }

    // 获取 callback Token。
    public String getCallbackToken() {
        // 返回当前保存的 callback Token。
        return callbackToken;
    }

    // 设置 callback Token。
    public void setCallbackToken(String callbackToken) {
        // 将传入值写入 callbackToken 字段。
        this.callbackToken = callbackToken;
    }

    // 获取 Webhooks 插件 route 路径。
    public String getTaskflowWebhookPath() {
        // 返回当前保存的 Webhooks 插件路径。
        return taskflowWebhookPath;
    }

    // 设置 Webhooks 插件 route 路径。
    public void setTaskflowWebhookPath(String taskflowWebhookPath) {
        // 将传入值写入 taskflowWebhookPath 字段。
        this.taskflowWebhookPath = taskflowWebhookPath;
    }

    // 获取调用 Webhooks 插件 route 时使用的共享密钥。
    public String getTaskflowWebhookSecret() {
        // 返回当前保存的 Webhooks 插件共享密钥。
        return taskflowWebhookSecret;
    }

    // 设置调用 Webhooks 插件 route 时使用的共享密钥。
    public void setTaskflowWebhookSecret(String taskflowWebhookSecret) {
        // 将传入值写入 taskflowWebhookSecret 字段。
        this.taskflowWebhookSecret = taskflowWebhookSecret;
    }
}
