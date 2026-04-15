// 声明当前 Webhook 控制器所在包。
package com.example.openclaw.web;

// 导入 OpenClaw 配置属性类。
import com.example.openclaw.config.OpenClawProperties;
// 导入 GET 映射注解。
import org.springframework.web.bind.annotation.GetMapping;
// 导入 POST 映射注解。
import org.springframework.web.bind.annotation.PostMapping;
// 导入请求体绑定注解。
import org.springframework.web.bind.annotation.RequestBody;
// 导入请求头绑定注解。
import org.springframework.web.bind.annotation.RequestHeader;
// 导入类级别路由注解。
import org.springframework.web.bind.annotation.RequestMapping;
// 导入 REST 控制器注解。
import org.springframework.web.bind.annotation.RestController;

// 导入有序 Map 实现。
import java.util.LinkedHashMap;
// 导入 Map 接口。
import java.util.Map;

// 标记当前类是 REST 控制器。
@RestController
// 统一定义 webhook 模块的访问前缀。
@RequestMapping("/api/webhook/openclaw")
public class OpenClawWebhookController {

    // 保存 OpenClaw 相关配置。
    private final OpenClawProperties properties;

    // 构造方法注入配置。
    public OpenClawWebhookController(OpenClawProperties properties) {
        // 保存配置属性对象。
        this.properties = properties;
    }

    // 提供一个 callback 接口，用于接收 OpenClaw 任务执行完成后的主动回调。
    @PostMapping("/callback")
    public Map<String, Object> callback(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Map<String, Object> payload
    ) {
        // 拼接期望的 Bearer Token。
        String expected = "Bearer " + properties.getCallbackToken();
        // 如果收到的 Token 与配置不匹配，则拒绝处理。
        if (!expected.equals(authorization)) {
            // 返回未授权响应。
            return Map.of("ok", false, "error", "unauthorized");
        }

        // 创建一个有序 Map，方便返回结构稳定、可读。
        Map<String, Object> result = new LinkedHashMap<>();
        // 标记处理成功。
        result.put("ok", true);
        // 返回模块类型，便于联调时识别。
        result.put("module", "webhook");
        // 返回 action 字段，帮助识别回调类型。
        result.put("receivedAction", payload.get("action"));
        // 返回 jobId，便于追踪后台任务。
        result.put("jobId", payload.get("jobId"));
        // 返回 summary，便于快速查看执行摘要。
        result.put("summary", payload.get("summary"));
        // 原样带回 payload，方便联调和扩展。
        result.put("payload", payload);
        // 返回最终响应。
        return result;
    }

    // 提供一个简单的健康检查接口。
    @GetMapping("/health")
    public Map<String, Object> health() {
        // 返回固定的健康状态与模块标识。
        return Map.of("ok", true, "module", "webhook", "service", "openclaw-spring-demo");
    }
}
