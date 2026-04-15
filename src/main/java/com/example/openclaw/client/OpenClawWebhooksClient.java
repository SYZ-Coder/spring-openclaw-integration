// 声明当前 Webhooks 客户端所在包。
package com.example.openclaw.client;

// 导入 OpenClaw 配置属性类。
import com.example.openclaw.config.OpenClawProperties;
// 导入 HTTP 请求头常量。
import org.springframework.http.HttpHeaders;
// 导入 HTTP 媒体类型常量。
import org.springframework.http.MediaType;
// 导入 Spring 组件注解。
import org.springframework.stereotype.Component;
// 导入 WebClient。
import org.springframework.web.reactive.function.client.WebClient;
// 导入响应式 Mono。
import reactor.core.publisher.Mono;

// 导入 Map 接口，用于承载 webhook 请求体。
import java.util.Map;

// 标记当前类是一个 Spring 组件。
@Component
public class OpenClawWebhooksClient {

    // 保存 OpenClaw 配置。
    private final OpenClawProperties properties;
    // 保存用于发送 HTTP 请求的 WebClient。
    private final WebClient webClient;

    // 构造方法注入配置和 WebClient。
    public OpenClawWebhooksClient(OpenClawProperties properties, WebClient openClawWebClient) {
        // 保存配置对象。
        this.properties = properties;
        // 保存 WebClient。
        this.webClient = openClawWebClient;
    }

    // 统一封装对 Webhooks 插件 route 的调用。
    public Mono<Map> invoke(Map<String, Object> body) {
        // 向配置中的 Webhooks 插件 route 发送 POST 请求。
        return webClient.post()
                // 指定插件 webhook 路径。
                .uri(properties.getTaskflowWebhookPath())
                // 指定请求体为 JSON。
                .contentType(MediaType.APPLICATION_JSON)
                // 按插件要求带上 Bearer 共享密钥。
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getTaskflowWebhookSecret())
                // 写入请求体。
                .bodyValue(body)
                // 发起请求。
                .retrieve()
                // 把返回结果读取成 Map。
                .bodyToMono(Map.class);
    }
}
