# openclaw_spring

## Open Source Project Notes

- License: MIT
- Status: experimental sample project for learning and integration reference
- Contributions: see `CONTRIBUTING.md`
- Security reporting: see `SECURITY.md`
- Community expectations: see `CODE_OF_CONDUCT.md`

这是一个独立的 Spring Boot 示例项目，用来演示如何集成 OpenClaw。

## 项目包含内容

- `POST /api/http/openclaw/responses`：HTTP 模块，非流式代理 OpenClaw `/v1/responses`
- `POST /api/http/openclaw/stream/raw`：HTTP 模块，透传 OpenClaw 返回的原始 SSE 数据块
- `POST /api/http/openclaw/stream/text`：HTTP 模块，从 SSE JSON 事件中提取文本并向外流式输出
- `POST /api/webhook/openclaw/callback`：Webhook 模块，接收 OpenClaw cron delivery 的回调
- `POST /api/taskflow/openclaw/create`：TaskFlow 模块，通过 OpenClaw `webhooks` 插件创建 TaskFlow
- `POST /api/taskflow/openclaw/list`：TaskFlow 模块，通过 OpenClaw `webhooks` 插件查询 TaskFlow 列表
- `POST /api/taskflow/openclaw/get`：TaskFlow 模块，通过 OpenClaw `webhooks` 插件查询指定 TaskFlow 详情
- `POST /api/taskflow/openclaw/run-task`：TaskFlow 模块，通过 OpenClaw `webhooks` 插件为 TaskFlow 创建子任务
- `POST /api/taskflow/openclaw/resume`：TaskFlow 模块，通过 OpenClaw `webhooks` 插件恢复 TaskFlow
- `POST /api/taskflow/openclaw/finish`：TaskFlow 模块，通过 OpenClaw `webhooks` 插件完成 TaskFlow
- `POST /api/taskflow/openclaw/fail`：TaskFlow 模块，通过 OpenClaw `webhooks` 插件将 TaskFlow 标记为失败
- `docs/openclaw-study.md`：基于 OpenClaw 源码和官方资料整理的架构学习文档
- `docs/openclaw-official-links.md`：OpenClaw 官方文档链接导航页，适合快速跳转和收藏
- `docs/openclaw-config-guide.md`：Spring Boot 配置与 OpenClaw 原生配置的对应说明
- `docs/webhooks-taskflow-guide.md`：`webhooks` 插件与 TaskFlow 的实现原理和落地说明

## 三个模块如何划分

### 1. HTTP 模块

用途：

- 让业务系统主动请求 OpenClaw
- 适合问答、分析、流式文本输出

对应控制器：

- `OpenClawHttpController`

对应接口：

- `POST /api/http/openclaw/responses`
- `POST /api/http/openclaw/stream/raw`
- `POST /api/http/openclaw/stream/text`

### 2. Webhook 模块

用途：

- 让 OpenClaw 在任务完成后主动回调你的系统
- 适合 cron webhook、异步任务完成通知

对应控制器：

- `OpenClawWebhookController`

对应接口：

- `POST /api/webhook/openclaw/callback`

### 3. TaskFlow 模块

用途：

- 让你的系统通过 `webhooks` 插件驱动 OpenClaw 内部 TaskFlow
- 适合有状态、可恢复、可追踪的流程编排

对应控制器：

- `OpenClawTaskFlowDemoController`

对应接口：

- `POST /api/taskflow/openclaw/create`
- `POST /api/taskflow/openclaw/list`
- `POST /api/taskflow/openclaw/get`
- `POST /api/taskflow/openclaw/run-task`
- `POST /api/taskflow/openclaw/resume`
- `POST /api/taskflow/openclaw/finish`
- `POST /api/taskflow/openclaw/fail`

## 三个模块的调用关系

```mermaid
flowchart LR
    A["业务系统"] --> B["HTTP 模块<br/>OpenClawHttpController"]
    C["OpenClaw Gateway"] --> D["Webhook 模块<br/>OpenClawWebhookController"]
    A --> E["TaskFlow 模块<br/>OpenClawTaskFlowDemoController"]
    E --> F["TaskFlow Service"]
    F --> G["TaskFlow Client"]
    G --> H["Webhooks Client"]
    H --> I["OpenClaw Webhooks Plugin Route"]
    I --> J["OpenClaw TaskFlow"]
```

说明：

- HTTP 模块是“你的系统主动请求 OpenClaw”
- Webhook 模块是“OpenClaw 主动回调你的系统”
- TaskFlow 模块是“你的系统经由 webhooks 插件驱动 OpenClaw TaskFlow”

## `hooks`、`webhooks` 插件、callback webhook 速查

这三个概念很容易混淆，建议团队按“谁先发起请求”来记。

| 概念 | 谁发起 | 典型路径 | 主要作用 |
| --- | --- | --- | --- |
| `hooks` | 外部系统发起到 OpenClaw | `/hooks/wake`、`/hooks/agent` | 触发一次 wake、agent turn 或通用事件 |
| `webhooks` 插件 | 外部系统发起到 OpenClaw | `/plugins/webhooks/<route>` | 通过插件 route 驱动 TaskFlow 动作 |
| callback webhook | OpenClaw 发起到外部系统 | 你的业务回调地址 | OpenClaw 把任务结果主动推送给外部系统 |

对应到本项目可以这样理解：

- `OpenClawHttpController`：你的系统主动调 OpenClaw
- `OpenClawWebhookController`：接收 OpenClaw 的 callback webhook
- `OpenClawTaskFlowDemoController`：你的系统通过 `webhooks` 插件去驱动 TaskFlow

如果要进一步区分：

- `hooks` 是 Gateway 自带的通用外部入口
- `webhooks` 插件是插件级入口，更适合对接 TaskFlow
- callback webhook 是 OpenClaw 的主动回调能力

## `webhooks` 和 `TaskFlow` 的代码职责

- `OpenClawWebhooksClient`：只负责调用 `webhooks` 插件 route
- `OpenClawTaskFlowClient`：只负责封装 TaskFlow 动作
- `OpenClawTaskFlowDemoService`：负责服务层编排

## 项目配置

编辑 `src/main/resources/application.yml`：

```yaml
openclaw:
  base-url: http://127.0.0.1:18789
  token: YOUR_GATEWAY_TOKEN
  callback-token: MY_CRON_WEBHOOK_TOKEN
  taskflow-webhook-path: /plugins/webhooks/zapier
  taskflow-webhook-secret: YOUR_WEBHOOK_PLUGIN_SECRET
```

## OpenClaw 服务端配置

上面的 `openclaw.*` 配置是这个 Spring Boot 示例项目自己的客户端配置。
它们在 OpenClaw 服务端分别对应 `gateway.*`、`cron.*`、`hooks.*` 这些原生配置。

一个最小可运行的 OpenClaw 服务端配置示例如下：

```json5
{
  gateway: {
    mode: "local",
    bind: "loopback",
    port: 18789,
    auth: {
      mode: "token",
      token: "YOUR_GATEWAY_TOKEN"
    },
    http: {
      endpoints: {
        chatCompletions: {
          enabled: true
        },
        responses: {
          enabled: true
        }
      }
    }
  },
  cron: {
    webhookToken: "MY_CRON_WEBHOOK_TOKEN"
  },
  hooks: {
    enabled: true,
    token: "MY_HOOK_TOKEN",
    path: "/hooks"
  }
}
```

说明：

- `gateway.http.endpoints.chatCompletions.enabled = true` 用于开启 `POST /v1/chat/completions`
- `gateway.http.endpoints.responses.enabled = true` 用于开启 `POST /v1/responses`
- `gateway.auth.token` 是 Spring Boot 服务访问 OpenClaw 时使用的 Bearer Token
- `cron.webhookToken` 是 OpenClaw 主动回调你的服务时使用的 Bearer Token
- `hooks.token` 是外部系统调用 `/hooks/wake` 或 `/hooks/agent` 时使用的 Token

进一步阅读：

- `docs/openclaw-config-guide.md`
- `docs/openclaw-study.md`
- `docs/webhooks-taskflow-guide.md`

## 启动项目

```bash
mvn spring-boot:run
```

## 每个接口是干什么用的

### HTTP 模块接口

- `POST /api/http/openclaw/responses`：请求 OpenClaw 返回完整结果
- `POST /api/http/openclaw/stream/raw`：查看 OpenClaw 原始 SSE 事件流
- `POST /api/http/openclaw/stream/text`：直接消费提取后的文本流

### Webhook 模块接口

- `POST /api/webhook/openclaw/callback`：接收 OpenClaw 的 cron/webhook 回调结果

### TaskFlow 模块接口

- `POST /api/taskflow/openclaw/create`：创建一个新的 TaskFlow
- `POST /api/taskflow/openclaw/list`：查询当前 route 下可见的 TaskFlow 列表
- `POST /api/taskflow/openclaw/get`：查询指定 TaskFlow 的详情
- `POST /api/taskflow/openclaw/run-task`：在指定 TaskFlow 下创建并启动子任务
- `POST /api/taskflow/openclaw/resume`：恢复一个待继续执行的 TaskFlow
- `POST /api/taskflow/openclaw/finish`：把一个 TaskFlow 标记为完成
- `POST /api/taskflow/openclaw/fail`：把一个 TaskFlow 标记为失败

## 测试 HTTP 模块非流式接口

```bash
curl -X POST http://localhost:8080/api/http/openclaw/responses \
  -H "Content-Type: application/json" \
  -d '{"prompt":"请总结今天的业务情况"}'
```

## 测试 HTTP 模块原始 SSE 透传

```bash
curl -N http://localhost:8080/api/http/openclaw/stream/raw \
  -H "Content-Type: application/json" \
  -d '{"prompt":"请流式输出今天的业务分析"}'
```

## 测试 HTTP 模块纯文本 SSE 流

```bash
curl -N http://localhost:8080/api/http/openclaw/stream/text \
  -H "Content-Type: application/json" \
  -d '{"prompt":"请流式输出今天的业务分析"}'
```

## 测试 Webhook 模块回调接收接口

```bash
curl -X POST http://localhost:8080/api/webhook/openclaw/callback \
  -H "Authorization: Bearer MY_CRON_WEBHOOK_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"action":"finished","jobId":"job-123","summary":"日报生成完成"}'
```

## 测试 TaskFlow 模块创建流程

```bash
curl -X POST http://localhost:8080/api/taskflow/openclaw/create \
  -H "Content-Type: application/json" \
  -d '{"goal":"Review inbound queue","controllerId":"webhooks/zapier"}'
```

## 测试 TaskFlow 模块查询列表

```bash
curl -X POST http://localhost:8080/api/taskflow/openclaw/list
```

## 测试 TaskFlow 模块查询详情

```bash
curl -X POST http://localhost:8080/api/taskflow/openclaw/get \
  -H "Content-Type: application/json" \
  -d '{"flowId":"flow_123"}'
```

## 测试 TaskFlow 模块运行子任务

```bash
curl -X POST http://localhost:8080/api/taskflow/openclaw/run-task \
  -H "Content-Type: application/json" \
  -d '{"flowId":"flow_123","runtime":"subagent","task":"Inspect the next message batch","childSessionKey":"agent:main:subagent:taskflow-demo"}'
```

## 测试 TaskFlow 模块恢复流程

```bash
curl -X POST http://localhost:8080/api/taskflow/openclaw/resume \
  -H "Content-Type: application/json" \
  -d '{"flowId":"flow_123"}'
```

## 测试 TaskFlow 模块完成流程

```bash
curl -X POST http://localhost:8080/api/taskflow/openclaw/finish \
  -H "Content-Type: application/json" \
  -d '{"flowId":"flow_123","summary":"任务已经全部处理完成"}'
```

## 测试 TaskFlow 模块标记失败

```bash
curl -X POST http://localhost:8080/api/taskflow/openclaw/fail \
  -H "Content-Type: application/json" \
  -d '{"flowId":"flow_123","reason":"外部依赖超时"}'
```

## 使用说明

- 新系统接 OpenClaw 时，优先选择 `/v1/responses`
- 文本流接口使用了防御式解析逻辑，用来兼容多种 OpenResponses 风格事件结构
- `webhooks` 插件 + TaskFlow 适合有状态、可恢复、可追踪的外部流程编排
- 生产环境中，建议把 OpenClaw 和这个 Spring Boot 服务都放在私网或受控入口后面，不要直接暴露 Gateway Token
