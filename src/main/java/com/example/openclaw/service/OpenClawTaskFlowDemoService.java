// 声明当前服务类所在包。
package com.example.openclaw.service;

// 导入 TaskFlow 动作客户端。
import com.example.openclaw.client.OpenClawTaskFlowClient;
// 导入 Spring 服务注解。
import org.springframework.stereotype.Service;
// 导入响应式 Mono。
import reactor.core.publisher.Mono;

// 导入 Map 接口。
import java.util.Map;

// 标记当前类是一个服务组件。
@Service
public class OpenClawTaskFlowDemoService {

    // 保存调用 TaskFlow 的客户端。
    private final OpenClawTaskFlowClient taskFlowClient;

    // 构造方法注入客户端。
    public OpenClawTaskFlowDemoService(OpenClawTaskFlowClient taskFlowClient) {
        // 保存客户端对象。
        this.taskFlowClient = taskFlowClient;
    }

    // 创建一个示例流程。
    public Mono<Map> createFlow(String goal, String controllerId) {
        // 把参数透传给 Webhooks 插件客户端。
        return taskFlowClient.createFlow(goal, controllerId);
    }

    // 查询当前 route 可见的所有流程。
    public Mono<Map> listFlows() {
        // 调用 list_flows 动作。
        return taskFlowClient.listFlows();
    }

    // 查询某个指定 flow 的详情。
    public Mono<Map> getFlow(String flowId) {
        // 调用 get_flow 动作。
        return taskFlowClient.getFlow(flowId);
    }

    // 在指定流程下创建子任务。
    public Mono<Map> runTask(String flowId, String runtime, String task, String childSessionKey) {
        // 调用 run_task 动作。
        return taskFlowClient.runTask(flowId, runtime, task, childSessionKey);
    }

    // 恢复某个 flow 的执行。
    public Mono<Map> resumeFlow(String flowId) {
        // 调用 resume_flow 动作。
        return taskFlowClient.resumeFlow(flowId);
    }

    // 将某个 flow 标记为完成。
    public Mono<Map> finishFlow(String flowId, String summary) {
        // 调用 finish_flow 动作。
        return taskFlowClient.finishFlow(flowId, summary);
    }

    // 将某个 flow 标记为失败。
    public Mono<Map> failFlow(String flowId, String reason) {
        // 调用 fail_flow 动作。
        return taskFlowClient.failFlow(flowId, reason);
    }
}
