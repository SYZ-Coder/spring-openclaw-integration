// 声明当前控制器所在包。
package com.example.openclaw.web;

// 导入 TaskFlow 示例服务。
import com.example.openclaw.service.OpenClawTaskFlowDemoService;
// 导入 REST 控制器注解。
import org.springframework.web.bind.annotation.PostMapping;
// 导入请求体注解。
import org.springframework.web.bind.annotation.RequestBody;
// 导入类级别路由注解。
import org.springframework.web.bind.annotation.RequestMapping;
// 导入 REST 控制器注解。
import org.springframework.web.bind.annotation.RestController;
// 导入响应式 Mono。
import reactor.core.publisher.Mono;

// 导入 Map 接口。
import java.util.Map;

// 标记当前类是 REST 控制器。
@RestController
// 统一定义 TaskFlow 模块接口前缀。
@RequestMapping("/api/taskflow/openclaw")
public class OpenClawTaskFlowDemoController {

    // 保存 TaskFlow 示例服务。
    private final OpenClawTaskFlowDemoService demoService;

    // 构造方法注入服务。
    public OpenClawTaskFlowDemoController(OpenClawTaskFlowDemoService demoService) {
        // 保存服务对象。
        this.demoService = demoService;
    }

    // 提供一个创建流程的演示接口。
    @PostMapping("/create")
    public Mono<Map> create(@RequestBody Map<String, String> req) {
        // 从请求体读取流程目标描述。
        String goal = req.getOrDefault("goal", "Review inbound queue");
        // 从请求体读取控制器 ID。
        String controllerId = req.getOrDefault("controllerId", "webhooks/zapier");
        // 调用服务创建流程。
        return demoService.createFlow(goal, controllerId);
    }

    // 提供一个列出流程的演示接口。
    @PostMapping("/list")
    public Mono<Map> list() {
        // 调用服务查询流程列表。
        return demoService.listFlows();
    }

    // 提供一个查询指定 flow 详情的演示接口。
    @PostMapping("/get")
    public Mono<Map> get(@RequestBody Map<String, String> req) {
        // 从请求体读取 flowId。
        String flowId = req.getOrDefault("flowId", "");
        // 如果 flowId 为空，则直接返回错误提示，避免向下游发送无效请求。
        if (!hasText(flowId)) {
            // 返回统一的错误结构。
            return Mono.just(Map.of("ok", false, "error", "flowId不能为空"));
        }
        // 调用服务查询 flow 详情。
        return demoService.getFlow(flowId);
    }

    // 提供一个创建子任务的演示接口。
    @PostMapping("/run-task")
    public Mono<Map> runTask(@RequestBody Map<String, String> req) {
        // 从请求体读取 flowId。
        String flowId = req.getOrDefault("flowId", "");
        // 从请求体读取 runtime，默认使用 subagent。
        String runtime = req.getOrDefault("runtime", "subagent");
        // 从请求体读取任务描述。
        String task = req.getOrDefault("task", "Inspect the next message batch");
        // 从请求体读取子会话键。
        String childSessionKey = req.getOrDefault("childSessionKey", "agent:main:subagent:taskflow-demo");
        // 如果 flowId 为空，则直接返回错误提示。
        if (!hasText(flowId)) {
            // 返回统一的错误结构。
            return Mono.just(Map.of("ok", false, "error", "flowId不能为空"));
        }
        // 调用服务创建并启动子任务。
        return demoService.runTask(flowId, runtime, task, childSessionKey);
    }

    // 提供一个恢复 flow 的演示接口。
    @PostMapping("/resume")
    public Mono<Map> resume(@RequestBody Map<String, String> req) {
        // 从请求体读取 flowId。
        String flowId = req.getOrDefault("flowId", "");
        // 如果 flowId 为空，则直接返回错误提示。
        if (!hasText(flowId)) {
            // 返回统一的错误结构。
            return Mono.just(Map.of("ok", false, "error", "flowId不能为空"));
        }
        // 调用服务恢复 flow。
        return demoService.resumeFlow(flowId);
    }

    // 提供一个完成 flow 的演示接口。
    @PostMapping("/finish")
    public Mono<Map> finish(@RequestBody Map<String, String> req) {
        // 从请求体读取 flowId。
        String flowId = req.getOrDefault("flowId", "");
        // 从请求体读取完成摘要，默认给出示例文本。
        String summary = req.getOrDefault("summary", "TaskFlow执行完成");
        // 如果 flowId 为空，则直接返回错误提示。
        if (!hasText(flowId)) {
            // 返回统一的错误结构。
            return Mono.just(Map.of("ok", false, "error", "flowId不能为空"));
        }
        // 调用服务将 flow 标记为完成。
        return demoService.finishFlow(flowId, summary);
    }

    // 提供一个失败 flow 的演示接口。
    @PostMapping("/fail")
    public Mono<Map> fail(@RequestBody Map<String, String> req) {
        // 从请求体读取 flowId。
        String flowId = req.getOrDefault("flowId", "");
        // 从请求体读取失败原因，默认给出示例文本。
        String reason = req.getOrDefault("reason", "TaskFlow执行失败");
        // 如果 flowId 为空，则直接返回错误提示。
        if (!hasText(flowId)) {
            // 返回统一的错误结构。
            return Mono.just(Map.of("ok", false, "error", "flowId不能为空"));
        }
        // 调用服务将 flow 标记为失败。
        return demoService.failFlow(flowId, reason);
    }

    // 提供一个简单的健康检查接口。
    @PostMapping("/health")
    public Map<String, Object> health() {
        // 返回固定的健康状态与模块标识。
        return Map.of("ok", true, "module", "taskflow", "service", "openclaw-spring-demo");
    }

    // 判断字符串是否包含有效文本，避免空串和纯空白串进入下游流程。
    private boolean hasText(String value) {
        // 返回判空结果。
        return value != null && !value.trim().isEmpty();
    }
}
