// 声明当前 TaskFlow 客户端所在包。
package com.example.openclaw.client;

// 导入 Spring 组件注解。
import org.springframework.stereotype.Component;
// 导入响应式 Mono。
import reactor.core.publisher.Mono;

// 导入 Map 接口，用于构造 TaskFlow 动作体。
import java.util.Map;

// 标记当前类是一个 Spring 组件。
@Component
public class OpenClawTaskFlowClient {

    // 保存底层的 Webhooks 客户端。
    private final OpenClawWebhooksClient webhooksClient;

    // 构造方法注入 Webhooks 客户端。
    public OpenClawTaskFlowClient(OpenClawWebhooksClient webhooksClient) {
        // 保存底层客户端。
        this.webhooksClient = webhooksClient;
    }

    // 创建一个新的 TaskFlow。
    public Mono<Map> createFlow(String goal, String controllerId) {
        // 构造 create_flow 动作请求体。
        Map<String, Object> body = Map.of(
                // 指定动作类型。
                "action", "create_flow",
                // 指定流程目标描述。
                "goal", goal,
                // 指定控制器标识。
                "controllerId", controllerId,
                // 指定初始状态。
                "status", "queued",
                // 指定通知策略。
                "notifyPolicy", "done_only"
        );

        // 交给 Webhooks 客户端统一发送。
        return webhooksClient.invoke(body);
    }

    // 查询当前 route 下可见的所有流程。
    public Mono<Map> listFlows() {
        // 构造 list_flows 动作请求体。
        Map<String, Object> body = Map.of(
                // 指定动作类型。
                "action", "list_flows"
        );

        // 交给 Webhooks 客户端统一发送。
        return webhooksClient.invoke(body);
    }

    // 查询指定 flow 的详情。
    public Mono<Map> getFlow(String flowId) {
        // 构造 get_flow 动作请求体。
        Map<String, Object> body = Map.of(
                // 指定动作类型。
                "action", "get_flow",
                // 指定要查询的 flowId。
                "flowId", flowId
        );

        // 交给 Webhooks 客户端统一发送。
        return webhooksClient.invoke(body);
    }

    // 在指定 flow 下创建并运行一个子任务。
    public Mono<Map> runTask(String flowId, String runtime, String task, String childSessionKey) {
        // 构造 run_task 动作请求体。
        Map<String, Object> body = Map.of(
                // 指定动作类型。
                "action", "run_task",
                // 指定 flowId。
                "flowId", flowId,
                // 指定 runtime。
                "runtime", runtime,
                // 指定任务描述。
                "task", task,
                // 指定子会话键。
                "childSessionKey", childSessionKey,
                // 指定初始状态。
                "status", "queued",
                // 指定通知策略。
                "notifyPolicy", "state_changes"
        );

        // 交给 Webhooks 客户端统一发送。
        return webhooksClient.invoke(body);
    }

    // 恢复指定 flow。
    public Mono<Map> resumeFlow(String flowId) {
        // 构造 resume_flow 动作请求体。
        Map<String, Object> body = Map.of(
                // 指定动作类型。
                "action", "resume_flow",
                // 指定 flowId。
                "flowId", flowId
        );

        // 交给 Webhooks 客户端统一发送。
        return webhooksClient.invoke(body);
    }

    // 将指定 flow 标记为完成。
    public Mono<Map> finishFlow(String flowId, String summary) {
        // 构造 finish_flow 动作请求体。
        Map<String, Object> body = Map.of(
                // 指定动作类型。
                "action", "finish_flow",
                // 指定 flowId。
                "flowId", flowId,
                // 指定完成摘要。
                "summary", summary
        );

        // 交给 Webhooks 客户端统一发送。
        return webhooksClient.invoke(body);
    }

    // 将指定 flow 标记为失败。
    public Mono<Map> failFlow(String flowId, String reason) {
        // 构造 fail_flow 动作请求体。
        Map<String, Object> body = Map.of(
                // 指定动作类型。
                "action", "fail_flow",
                // 指定 flowId。
                "flowId", flowId,
                // 指定失败原因。
                "reason", reason
        );

        // 交给 Webhooks 客户端统一发送。
        return webhooksClient.invoke(body);
    }
}
