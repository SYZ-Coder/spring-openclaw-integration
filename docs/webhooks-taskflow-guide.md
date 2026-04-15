# Webhooks 插件与 TaskFlow 实现说明

## 1. 这份文档解决什么问题

这份文档专门说明两种能力如何实现：

- OpenClaw `webhooks` 插件如何工作
- OpenClaw `TaskFlow` 如何被外部系统驱动

同时，本文件也说明项目中新增加的 Spring Boot 示例代码如何对应这些能力。

---

## 2. 两种方式的关系

很多时候会把 `webhooks` 插件和 `TaskFlow` 当成两套独立方案，但它们更准确的关系是：

- `TaskFlow` 是 OpenClaw 的任务流转和状态管理能力
- `webhooks` 插件是外部系统访问 TaskFlow 的一个官方桥接层

也就是说：

- `TaskFlow` 是内部能力
- `webhooks` 插件是外部入口

---

## 3. 实现原理

### 3.1 Webhooks 插件的原理

`webhooks` 插件会在 Gateway 内部注册一条或多条 HTTP route。

这些 route 的关键配置通常包括：

- `path`
- `sessionKey`
- `secret`
- `controllerId`

当外部系统调用某一条 route 时，大致过程是：

1. Gateway 收到 HTTP 请求
2. `webhooks` 插件校验共享密钥
3. 插件把 route 绑定到某个固定 `sessionKey`
4. 插件把请求转换成 TaskFlow 动作
5. TaskFlow 执行对应的流程操作
6. Gateway 返回 JSON 结果给外部系统

因此它的本质是：

外部系统 -> HTTP -> Webhooks 插件 -> TaskFlow

### 3.2 TaskFlow 的原理

TaskFlow 可以理解为 OpenClaw 中的任务流状态机。

它负责管理：

- flow 的创建
- flow 的状态
- flow 下子任务的运行
- 等待、恢复、完成、失败、取消

常见动作包括：

- `create_flow`
- `list_flows`
- `get_flow`
- `run_task`
- `set_waiting`
- `resume_flow`
- `finish_flow`
- `fail_flow`
- `cancel_flow`

如果说 cron 更偏“时间触发”，那么 TaskFlow 更偏“流程驱动”。

---

## 4. `webhook` 回调、`hooks`、`webhooks` 插件的区别

这一组概念最容易混淆，所以这里单独拆开说明。

### 4.1 `webhook` 回调是谁触发的

如果说的是本项目中的 `POST /api/webhook/openclaw/callback`，触发者是：

- OpenClaw Gateway
- 更准确地说，是 OpenClaw 的 cron / job delivery 机制

也就是说，当 OpenClaw 中某个任务执行完成，并且它的 `delivery.mode = "webhook"` 时，OpenClaw 会主动向你的 Spring Boot 接口发起一个 `POST` 请求。

调用方向如下：

```mermaid
sequenceDiagram
    participant J as "OpenClaw Job / Cron"
    participant G as "OpenClaw Gateway"
    participant B as "Spring Boot Callback"

    J->>G: 任务执行完成
    G->>G: 检查 delivery.mode = webhook
    G->>B: POST /api/webhook/openclaw/callback
    B-->>G: 200 OK
```

这条链路里，Spring Boot 是被动接收方，不是主动发起方。

### 4.2 OpenClaw 如何配置 callback webhook

callback webhook 不是通过 `webhooks` 插件配置出来的，而是通过 cron / job delivery 配置出来的。

通常需要两层配置：

#### 1. 全局 webhook token

```json5
{
  cron: {
    webhookToken: "MY_CRON_WEBHOOK_TOKEN"
  }
}
```

这表示：

- OpenClaw 主动回调外部系统时
- 会带上 `Authorization: Bearer MY_CRON_WEBHOOK_TOKEN`

#### 2. 具体任务的 delivery 配置

```json5
{
  name: "daily-report",
  sessionTarget: "isolated",
  payload: {
    kind: "agentTurn",
    message: "请生成今天的业务日报"
  },
  delivery: {
    mode: "webhook",
    to: "http://127.0.0.1:8080/api/webhook/openclaw/callback"
  }
}
```

这表示：

- 任务执行完之后
- OpenClaw 要把结果主动推送到指定 URL

### 4.3 三者的核心区别

团队落地时，最简单的理解方法是看“谁先发起请求”。

| 概念 | 谁发起 | 典型路径 | 主要作用 | 适合场景 |
| --- | --- | --- | --- | --- |
| `hooks` | 外部系统发起到 OpenClaw | `/hooks/wake`、`/hooks/agent`、`/hooks/<name>` | 触发一次 wake、agent turn 或通用事件 | 外部系统临时触发 OpenClaw 做一次事 |
| `webhooks` 插件 | 外部系统发起到 OpenClaw | `/plugins/webhooks/<route>` | 通过插件 route 驱动 TaskFlow 动作 | 外部系统编排有状态流程 |
| callback webhook | OpenClaw 发起到外部系统 | 你的业务回调地址 | OpenClaw 把任务结果主动推送给外部系统 | cron 回调、异步任务完成通知 |

可以进一步这样记：

- `hooks`：外部系统通知 OpenClaw“做一次事”
- `webhooks` 插件：外部系统通过插件 route 精细驱动 TaskFlow
- callback webhook：OpenClaw 执行完后，反过来通知你的系统

### 4.4 `hooks` 和 `webhooks` 插件在配置上有什么不同

#### `hooks` 的配置

`hooks` 是 Gateway 自带的通用 HTTP 入口，配置重点通常是：

```json5
{
  hooks: {
    enabled: true,
    token: "MY_HOOK_TOKEN",
    path: "/hooks"
  }
}
```

含义是：

- 开启 `/hooks/*` 入口
- 外部系统调用时需要带 Bearer Token
- 常用于 `/hooks/wake`、`/hooks/agent`

#### `webhooks` 插件的配置

`webhooks` 插件是插件级 route，配置重点通常是：

```json5
{
  plugins: {
    entries: {
      webhooks: {
        enabled: true,
        config: {
          routes: {
            zapier: {
              path: "/plugins/webhooks/zapier",
              sessionKey: "agent:main:main",
              controllerId: "webhooks/zapier",
              secret: {
                source: "inline",
                value: "YOUR_WEBHOOK_PLUGIN_SECRET"
              }
            }
          }
        }
      }
    }
  }
}
```

含义是：

- 暴露插件自己的 HTTP route
- 用共享密钥保护 route
- 把请求绑定到指定 session / controller
- 更适合转成 TaskFlow 动作

因此：

- `hooks` 更像 Gateway 的通用外部入口
- `webhooks` 插件更像 TaskFlow 的插件化桥接入口

---

## 5. OpenClaw 侧需要准备什么

如果要让本项目里的 Spring Boot 示例真正调通 `webhooks` 插件和 TaskFlow，OpenClaw 侧至少要准备一条插件 route。

示意配置如下：

```json5
{
  plugins: {
    entries: {
      webhooks: {
        enabled: true,
        config: {
          routes: {
            zapier: {
              path: "/plugins/webhooks/zapier",
              sessionKey: "agent:main:main",
              controllerId: "webhooks/zapier",
              description: "TaskFlow bridge for external systems",
              secret: {
                source: "inline",
                value: "YOUR_WEBHOOK_PLUGIN_SECRET"
              }
            }
          }
        }
      }
    }
  }
}
```

这段配置表达的是：

- 对外暴露一条 `/plugins/webhooks/zapier` 路由
- 这条路由绑定到一个固定的 `sessionKey`
- 外部系统必须带上共享密钥才能访问
- 进入后由 `webhooks` 插件把动作转给 TaskFlow

---

## 6. 代码如何拆分

为了让团队能明显看出 `http`、`webhook`、`taskflow` 三个方向分别怎么调用，本项目现在分成了三类控制器和两类客户端。

### 5.1 控制器层拆分

- `OpenClawHttpController`：负责普通 HTTP 请求和 SSE 请求
- `OpenClawWebhookController`：负责接收 OpenClaw 主动回调
- `OpenClawTaskFlowDemoController`：负责通过 `webhooks` 插件驱动 TaskFlow

### 5.2 客户端层拆分

- `OpenClawWebhooksClient`：只负责调用 `webhooks` 插件 route
- `OpenClawTaskFlowClient`：只负责封装 TaskFlow 动作

### 5.3 服务层

- `OpenClawTaskFlowDemoService`：负责把控制器和 TaskFlow 客户端连接起来

---

## 7. 三个模块的调用关系

```mermaid
flowchart LR
    A["业务系统"] --> B["HTTP Controller"]
    C["OpenClaw Gateway"] --> D["Webhook Controller"]
    A --> E["TaskFlow Controller"]
    E --> F["TaskFlow Service"]
    F --> G["TaskFlow Client"]
    G --> H["Webhooks Client"]
    H --> I["OpenClaw Webhooks Plugin Route"]
    I --> J["OpenClaw TaskFlow"]
```

说明：

- HTTP Controller 对应“你的系统主动请求 OpenClaw”
- Webhook Controller 对应“OpenClaw 主动回调你的系统”
- TaskFlow Controller 对应“你的系统通过 `webhooks` 插件驱动 TaskFlow”

---

## 8. 每个接口是干什么用的

### 7.1 HTTP 模块接口

- `POST /api/http/openclaw/responses`：请求 OpenClaw 返回完整结果
- `POST /api/http/openclaw/stream/raw`：查看 OpenClaw 原始 SSE 事件流
- `POST /api/http/openclaw/stream/text`：直接消费提取后的文本流

### 7.2 Webhook 模块接口

- `POST /api/webhook/openclaw/callback`：接收 OpenClaw 的 cron/webhook 回调结果

### 7.3 TaskFlow 模块接口

- `POST /api/taskflow/openclaw/create`：创建一个新的 TaskFlow
- `POST /api/taskflow/openclaw/list`：查询当前 route 下可见的 TaskFlow 列表
- `POST /api/taskflow/openclaw/get`：查询指定 TaskFlow 的详情
- `POST /api/taskflow/openclaw/run-task`：在指定 TaskFlow 下创建并启动子任务
- `POST /api/taskflow/openclaw/resume`：恢复一个待继续执行的 TaskFlow
- `POST /api/taskflow/openclaw/finish`：把一个 TaskFlow 标记为完成
- `POST /api/taskflow/openclaw/fail`：把一个 TaskFlow 标记为失败

---

## 9. `webhooks` 和 `TaskFlow` 在代码里分别在哪

### 8.1 `webhooks` 插件调用实现

在项目里，`webhooks` 调用的核心代码是：

- `src/main/java/com/example/openclaw/client/OpenClawWebhooksClient.java`

它只负责：

- 组装对插件 route 的 HTTP POST 请求
- 带上 `Authorization: Bearer <secret>`
- 把任意动作体发送给 `/plugins/webhooks/...`

### 8.2 TaskFlow 动作实现

在项目里，TaskFlow 动作封装的核心代码是：

- `src/main/java/com/example/openclaw/client/OpenClawTaskFlowClient.java`

它只负责：

- 把 `create_flow`、`list_flows`、`get_flow`、`run_task`、`resume_flow`、`finish_flow`、`fail_flow` 这些动作分别封装成方法
- 最后调用 `OpenClawWebhooksClient`

---

## 10. 典型调用链

### 9.1 业务系统主动请求 OpenClaw

```mermaid
sequenceDiagram
    participant S as Spring Boot Client
    participant H as HTTP Controller
    participant O as OpenClaw HTTP API

    S->>H: POST /api/http/openclaw/responses
    H->>O: POST /v1/responses
    O-->>H: JSON 或 SSE
    H-->>S: 返回结果
```

### 9.2 OpenClaw 主动回调业务系统

```mermaid
sequenceDiagram
    participant O as OpenClaw Gateway
    participant W as Webhook Controller

    O->>W: POST /api/webhook/openclaw/callback
    W->>W: 校验 callback token
    W-->>O: 200 OK + 回执结构
```

### 9.3 业务系统驱动 TaskFlow

```mermaid
sequenceDiagram
    participant S as Spring Boot Client
    participant C as TaskFlow Controller
    participant V as TaskFlow Service
    participant T as TaskFlow Client
    participant W as Webhooks Client
    participant P as OpenClaw Webhooks Plugin
    participant F as OpenClaw TaskFlow

    S->>C: POST /api/taskflow/openclaw/create
    C->>V: 调用服务
    V->>T: createFlow(...)
    T->>W: invoke(action body)
    W->>P: POST /plugins/webhooks/zapier
    P->>F: create_flow
    F-->>P: 返回结果
    P-->>W: JSON
    W-->>T: JSON
    T-->>V: JSON
    V-->>C: JSON
    C-->>S: JSON
```

---

## 11. 代码位置

本项目中相关代码位置：

- `src/main/resources/application.yml`
- `src/main/java/com/example/openclaw/web/OpenClawHttpController.java`
- `src/main/java/com/example/openclaw/web/OpenClawWebhookController.java`
- `src/main/java/com/example/openclaw/web/OpenClawTaskFlowDemoController.java`
- `src/main/java/com/example/openclaw/client/OpenClawWebhooksClient.java`
- `src/main/java/com/example/openclaw/client/OpenClawTaskFlowClient.java`
- `src/main/java/com/example/openclaw/service/OpenClawTaskFlowDemoService.java`

---

## 12. 推荐落地路线

团队如果要逐步接入，建议顺序如下：

1. 先用 `/v1/responses` 做最小接入
2. 再用 cron webhook 做结果回调
3. 再引入 `webhooks` 插件
4. 再把复杂任务改造成 TaskFlow

这样好处是：

- 上手简单
- 风险可控
- 调试路径清晰
- 可以逐步演进
