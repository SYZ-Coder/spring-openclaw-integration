// 声明当前 HTTP 控制器所在包。
package com.example.openclaw.web;

// 导入 OpenClaw responses 客户端。
import com.example.openclaw.client.OpenClawResponsesClient;
// 导入 SSE 文本提取服务。
import com.example.openclaw.service.OpenClawSseTextAssembler;
// 导入 HTTP 媒体类型常量。
import org.springframework.http.MediaType;
// 导入 GET 映射注解。
import org.springframework.web.bind.annotation.GetMapping;
// 导入 POST 映射注解。
import org.springframework.web.bind.annotation.PostMapping;
// 导入请求体绑定注解。
import org.springframework.web.bind.annotation.RequestBody;
// 导入类级别路由注解。
import org.springframework.web.bind.annotation.RequestMapping;
// 导入 REST 控制器注解。
import org.springframework.web.bind.annotation.RestController;
// 导入响应式 Flux。
import reactor.core.publisher.Flux;
// 导入响应式 Mono。
import reactor.core.publisher.Mono;

// 导入 Map 接口。
import java.util.Map;

// 标记当前类是 REST 控制器。
@RestController
// 统一定义 HTTP 模块的访问前缀。
@RequestMapping("/api/http/openclaw")
public class OpenClawHttpController {

    // 保存 OpenClaw HTTP 调用客户端。
    private final OpenClawResponsesClient responsesClient;
    // 保存 SSE 文本提取服务。
    private final OpenClawSseTextAssembler textAssembler;

    // 构造方法，用于依赖注入。
    public OpenClawHttpController(
            OpenClawResponsesClient responsesClient,
            OpenClawSseTextAssembler textAssembler
    ) {
        // 保存 OpenClaw 调用客户端。
        this.responsesClient = responsesClient;
        // 保存 SSE 文本提取器。
        this.textAssembler = textAssembler;
    }

    // 提供一个非流式代理接口，用来请求 OpenClaw `/v1/responses` 并获取完整结果。
    @PostMapping("/responses")
    public Mono<Map> responses(@RequestBody Map<String, String> req) {
        // 从请求体读取 prompt，如果没有则使用默认提示词。
        String prompt = req.getOrDefault("prompt", "请总结今天的业务情况。");
        // 调用 OpenClaw 非流式接口并直接返回结果。
        return responsesClient.createResponse(prompt);
    }

    // 提供一个 raw SSE 透传接口，用来观察 OpenClaw 原始流式事件。
    @PostMapping(value = "/stream/raw", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamRaw(@RequestBody Map<String, String> req) {
        // 从请求体读取 prompt，如果没有则使用默认提示词。
        String prompt = req.getOrDefault("prompt", "请流式输出今天的业务分析。");
        // 调用 OpenClaw 原始流式接口，并重新包装成标准 SSE 文本。
        return responsesClient.streamRawEvents(prompt)
                // 给每个返回片段加上 `data:` 前缀与双换行。
                .map(line -> "data: " + line + "\n\n");
    }

    // 提供一个 text-only SSE 接口，直接输出可展示的文本片段。
    @PostMapping(value = "/stream/text", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamText(@RequestBody Map<String, String> req) {
        // 从请求体读取 prompt，如果没有则使用默认提示词。
        String prompt = req.getOrDefault("prompt", "请流式输出今天的业务分析。");
        // 先拿到原始事件流，再提取文本片段，最后重新封装为 SSE。
        return textAssembler.extractTextChunks(responsesClient.streamRawEvents(prompt))
                // 给每个文本片段加上 `data:` 前缀与双换行。
                .map(chunk -> "data: " + chunk + "\n\n");
    }

    // 提供一个简单的健康检查接口。
    @GetMapping("/health")
    public Map<String, Object> health() {
        // 返回固定的健康状态与模块标识。
        return Map.of("ok", true, "module", "http", "service", "openclaw-spring-demo");
    }
}
