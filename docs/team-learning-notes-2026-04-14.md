# OpenClaw + Spring Boot 学习整理（2026-04-14）

## 1. 文档目的

这份文档用于沉淀本次交流中的核心结论、示例代码方向、架构理解、集成建议和落地注意事项，方便团队后续学习、共享和持续补充。

本文不是逐句聊天记录导出，而是对本次问答内容进行结构化整理。

---

## 2. 本次学习目标

本轮工作主要围绕以下几个问题展开：

- 基于源码和官方文档分析 OpenClaw 的整体架构设计
- 理解 OpenClaw 的 hook 机制、插件机制、Gateway 扩展能力
- 梳理外部服务如何请求 OpenClaw 并接收结果
- 梳理 OpenClaw 如何主动调用外部服务并推送数据
- 给出一套 Spring Boot 对接 OpenClaw 的可运行示例
- 给出 Java 使用 `WebClient` + SSE 流式接收 `/v1/responses` 的代码示例
- 将分析内容和示例项目一起沉淀到单独目录，便于团队学习

---

## 3. OpenClaw 的核心定位

从源码和官方资料来看，OpenClaw 的定位不是单一机器人程序，而是一个统一 AI Gateway 平台。

它把下列能力集中到了同一个 Gateway 进程：

- WebSocket 控制面
- HTTP 兼容接口
- Agent 执行
- 多消息渠道路由
- 插件扩展
- hooks / webhooks
- cron / TaskFlow 自动化

换句话说，OpenClaw 更像一个：

- AI control plane
- multi-channel messaging runtime
- automation hub
- plugin host

而不是单纯的大模型包装器。

---

## 4. OpenClaw 总体架构理解

### 4.1 总体分层

OpenClaw 可以理解为 5 层：

1. Gateway / Transport 层
2. Agent 执行层
3. Channels 消息层
4. Plugin 扩展层
5. Automation 自动化层

### 4.2 Gateway / Transport 层

Gateway 是 OpenClaw 的核心长生命周期进程，统一承载：

- WebSocket 协议
- HTTP `/v1/*` 接口
- Control UI
- hooks / webhook 入口
- 节点设备接入

特点：

- 单端口复用 WS + HTTP
- 控制面和运行面统一
- 所有客户端通过统一协议接入

### 4.3 Agent 执行层

OpenClaw 的 `/v1/chat/completions` 与 `/v1/responses` 本质上都不是独立推理服务，而是落到正常 agent run 路径。

这意味着：

- 外部调用与内部 agent 调用一致
- 工具调用、session、权限、hooks、plugins 都参与执行过程

### 4.4 Channels 消息层

OpenClaw 统一抽象了多消息渠道：

- Telegram
- Discord
- Slack
- Signal
- iMessage
- WhatsApp Web
- 以及其他插件渠道

Core 负责：

- 通用消息工具 host
- session / thread 绑定
- 统一路由与投递

Plugin 负责：

- 各渠道自己的行为实现
- 各渠道自己的能力适配

### 4.5 Plugin 扩展层

插件系统是 OpenClaw 的重要基础设施。插件可以注册：

- provider
- channel
- tool
- hook
- service
- CLI command
- HTTP route

插件不是“附加脚本”，而是平台能力提供者。

### 4.6 Automation 自动化层

自动化面包括：

- internal hooks
- HTTP hooks / webhooks
- cron
- TaskFlow

重要理解：

OpenClaw 并没有把自动化做成独立旁路，而是让自动化和 agent/session 走同一个执行平面。

---

## 5. Hook 机制整理

OpenClaw 中至少有三类 hook 概念。

### 5.1 Internal hooks

这是 Gateway 内部事件脚本机制。

常见事件：

- `command:new`
- `command:reset`
- `command:stop`
- `session:compact:before`
- `session:compact:after`
- `session:patch`
- `gateway:startup`
- `message:received`
- `message:transcribed`
- `message:preprocessed`
- `message:sent`

它们更像平台生命周期回调。

### 5.2 Plugin hooks

这是插件层更深的 typed hooks，用于参与执行流程控制。

典型 hooks：

- `before_model_resolve`
- `before_prompt_build`
- `before_agent_start`
- `before_agent_reply`
- `llm_input`
- `llm_output`
- `before_tool_call`
- `after_tool_call`
- `message_received`
- `message_sending`
- `message_sent`
- `reply_dispatch`
- `gateway_start`
- `gateway_stop`

重点用途：

- 改写模型/Provider 路由
- 注入 prompt
- 工具调用审批与阻断
- 回复前短路和改写
- 消息发送前拦截
- 控制最终消息投递方式

### 5.3 HTTP hooks / Webhooks

这是供外部系统调用的 webhook 入口。

典型接口：

- `POST /hooks/wake`
- `POST /hooks/agent`
- `POST /hooks/<name>`

其中 `/hooks/agent` 的重要特点是：

- 它不是简单同步聊天接口
- 源码实现上会走 isolated agent turn / cron 风格流程
- 更适合后台自动化触发

---

## 6. 插件机制整理

### 6.1 插件系统的核心目标

OpenClaw 的插件系统目标是：

- 让 core 尽量不硬编码具体渠道和厂商行为
- 通过 registry 统一消费能力
- 保证 discovery、validation、runtime load 和 surface exposure 分层清晰

### 6.2 插件常见注册能力

常见注册项包括：

- `registerProvider`
- `registerChannel`
- `registerTool`
- `registerHook`
- `registerSpeechProvider`
- `registerMediaUnderstandingProvider`
- `registerWebSearchProvider`
- `registerHttpRoute`
- `registerCommand`
- `registerService`

### 6.3 插件形态

插件常见形态：

- plain-capability
- hybrid-capability
- hook-only
- non-capability

可理解为：

- 有些插件只提供一个 provider
- 有些插件同时提供多个能力
- 有些插件偏流程拦截
- 有些插件提供 HTTP route、工具或后台服务

---

## 7. Gateway 扩展能力整理

OpenClaw Gateway 的扩展能力主要有三类。

### 7.1 协议扩展

Gateway WebSocket 协议使用显式 schema 建模，请求、响应、事件是统一帧结构。

这使得：

- CLI
- Web UI
- App
- 自动化客户端
- node / device

都可以共用同一控制协议。

### 7.2 HTTP 兼容扩展

Gateway 内建兼容接口：

- `GET /v1/models`
- `GET /v1/models/{id}`
- `POST /v1/embeddings`
- `POST /v1/chat/completions`
- `POST /v1/responses`

其中：

- `/v1/chat/completions` 适合老生态客户端
- `/v1/responses` 更适合新系统和 agent-native 集成

### 7.3 Plugin HTTP Route 扩展

插件可以通过 `registerHttpRoute(...)` 把自己的 HTTP 路由挂进 Gateway。

关键权限边界：

- `auth: "gateway"`：走 Gateway auth / runtime scope
- `auth: "plugin"`：适合插件自己做验签，不默认继承高权限

这个边界很重要，避免插件 route 默认成为高权限管理入口。

---

## 8. Webhooks 插件整理

官方 bundled plugin `webhooks` 的作用不是简单“接个 webhook”，而是：

- 让外部系统通过认证 HTTP route 驱动 OpenClaw TaskFlow
- 支持 Zapier、n8n、CI、内部调度系统集成
- 适合流程编排场景

支持的动作包括：

- `create_flow`
- `get_flow`
- `list_flows`
- `find_latest_flow`
- `resolve_flow`
- `get_task_summary`
- `set_waiting`
- `resume_flow`
- `finish_flow`
- `fail_flow`
- `request_cancel`
- `cancel_flow`
- `run_task`

理解重点：

这个插件更像“外部工作流驱动 OpenClaw 工作流状态机”的桥接层。

---

## 9. 外部系统如何请求 OpenClaw

### 9.1 推荐方式：`/v1/responses`

适合新系统，原因：

- 支持 item-based input
- 支持 SSE 流式输出
- 更接近 agent-native 模型

推荐用途：

- Java 后端
- 企业服务中台
- 工作流引擎
- 需要逐步扩展工具和富输入的系统

### 9.2 兼容方式：`/v1/chat/completions`

适合：

- Open WebUI
- LobeChat
- LibreChat
- 已有 OpenAI 客户端兼容层

### 9.3 深度控制方式：WebSocket Gateway Protocol

适合：

- 长连接控制面
- 事件订阅
- 设备 / node 能力接入
- 复杂客户端

---

## 10. OpenClaw 如何主动推送外部系统

官方内建方案是 cron 的 `delivery.mode = webhook`。

可选 delivery mode：

- `announce`
- `webhook`
- `none`

其中 `webhook` 的特点：

- OpenClaw 任务结束后主动 POST 外部服务
- 可以带 Bearer Token
- 适合日报、回调、异步结果投递

这意味着 OpenClaw 不只是被动服务端，也可以充当自动化任务执行后端。

---

## 11. Spring Boot 对接 OpenClaw 的设计结论

本次建议的落地架构是：

### 11.1 同步问答

Spring Boot 调用：

- `POST /v1/responses`

适合：

- 生成摘要
- 文本分析
- 智能问答
- 后台同步拿结果

### 11.2 兼容式问答

Spring Boot 调用：

- `POST /v1/chat/completions`

适合已有 OpenAI 兼容协议代码的场景。

### 11.3 异步回调

OpenClaw 通过：

- cron webhook delivery

主动把结果 POST 给 Spring Boot callback 接口。

### 11.4 更复杂自动化

如果将来需要更强的工作流能力：

- 接入 `webhooks` 插件
- 使用 TaskFlow 驱动复杂状态流程

---

## 12. 本次生成的 Spring Boot 示例项目内容

项目路径：

- `D:\spring_AI\openclaw_spring`

项目内容包括：

- Maven 工程
- `WebClient` 调用 OpenClaw `/v1/responses`
- SSE 原始流转发
- SSE 文本片段解析
- Spring Boot 接收 OpenClaw webhook callback
- OpenClaw 学习文档

核心接口：

- `POST /api/openclaw/responses`
- `POST /api/openclaw/stream/raw`
- `POST /api/openclaw/stream/text`
- `POST /api/openclaw/callback`
- `GET /api/openclaw/health`

---

## 13. Java 使用 WebClient + SSE 的关键实现要点

### 13.1 基本思路

Java 侧采用：

- Spring WebFlux
- `WebClient`
- `Flux<String>`

来接收 OpenClaw `/v1/responses` 的 SSE 输出。

### 13.2 接收 raw SSE

实现方式：

- `accept(text/event-stream)`
- `bodyToFlux(String.class)`
- 过滤空行
- 过滤 `[DONE]`

### 13.3 解析文本增量

由于流式输出可能出现多种 JSON 事件结构，所以示例里实现了一个“防御式解析器”，尝试从这些字段抽取文本：

- `delta`
- `output_text`
- `text`
- `response.output`
- `content`
- `item.content`

这样更适合学习和联调。

### 13.4 为什么要拆成 raw 和 text 两个接口

本次示例里同时保留：

- raw SSE
- text-only SSE

原因：

- raw SSE 适合调试 OpenClaw 实际返回事件结构
- text-only SSE 更适合前端直接展示

---

## 14. 本次踩到的问题和结论

### 14.1 文件编码问题

第一次生成 Java 文件后，Windows 下 Maven 编译报：

- UTF-8 BOM 非法字符

后续已修复为无 BOM UTF-8。

### 14.2 Maven 绑定错误 JRE

当前机器上：

- `java -version` 是 Java 20
- 但 `mvn -version` 实际使用的是 Java 8 JRE

导致 Spring Boot 3.3.2 编译失败。

结论：

- 项目代码本身已生成
- 真正阻塞点是本机 Maven 的 Java 运行时配置
- 需要把 Maven 切换到 JDK 17+

---

## 15. 团队建议的实施顺序

建议按以下顺序推进：

1. 先跑通 OpenClaw `/v1/responses`
2. 再跑通 Spring Boot 调用
3. 再验证 SSE 流式输出
4. 再接 webhook callback
5. 再上 cron 自动化
6. 最后再考虑 TaskFlow / webhooks 插件

这样团队学习成本最低，也最容易定位问题。

---

## 16. 建议的知识沉淀方向

后续团队可以继续往这份文档追加：

- OpenClaw 配置模板
- Java DTO 封装方式
- 前端消费 SSE 的示例
- 回调安全策略
- TaskFlow 使用案例
- 生产部署建议
- FAQ 和常见故障排查

推荐把本文作为团队的初始学习笔记，而不是最终文档终点。

---

## 17. 本次交付产物

### 17.1 项目文件

- `pom.xml`
- `src/main/resources/application.yml`
- `src/main/java/...`
- `README.md`

### 17.2 学习文档

- `docs/openclaw-study.md`
- `docs/team-learning-notes-2026-04-14.md`

其中：

- `openclaw-study.md` 更偏架构分析和图示
- `team-learning-notes-2026-04-14.md` 更偏本次问答沉淀和团队知识整理

---

## 18. 推荐后续动作

建议下一步继续做两件事：

1. 调整本机 Maven 到 JDK 17+
2. 跑通示例项目并完成一次真实 OpenClaw 联调

如果后续继续扩展，可以再补：

- DTO 版本 Java SDK 封装
- 前端 SSE 页面示例
- Docker / Nginx / 内网部署方案
- TaskFlow 场景样例
