# OpenClaw 官方文档链接导航

这份文档用于把团队最常用的 OpenClaw 官方资料整理成一页可点击导航，方便收藏、培训和日常查阅。

---

## 1. 总入口

- [OpenClaw 官方文档首页](https://docs.openclaw.ai/)
- [Gateway 文档入口](https://docs.openclaw.ai/gateway/index)
- [Plugins 文档入口](https://docs.openclaw.ai/plugins/architecture)

---

## 2. 架构与核心概念

- [总体架构 Architecture](https://docs.openclaw.ai/concepts/architecture)
- [Gateway Protocol](https://docs.openclaw.ai/gateway/protocol)
- [Plugins Architecture](https://docs.openclaw.ai/plugins/architecture)

适合：

- 想先理解 OpenClaw 是什么
- 想知道 Gateway、Plugin、TaskFlow、大致分层的人

---

## 3. Hooks、Webhooks、TaskFlow 相关

- [Hooks 官方页面](https://docs.openclaw.ai/automation/hooks)
- [Webhooks 插件官方页面](https://docs.openclaw.ai/plugins/webhooks)
- [Gateway Configuration Reference](https://docs.openclaw.ai/gateway/configuration-reference)

这里最容易混淆的是：

- `hooks` 页面主要讲 hooks 机制
- `webhooks` 插件页面主要讲插件 route 和 TaskFlow 桥接
- callback webhook / cron webhook delivery 更适合结合配置参考页查看

适合：

- 想区分 `hooks` 和 `webhooks` 插件
- 想查 `cron.webhookToken`
- 想确认 callback webhook 相关配置的人

---

## 4. HTTP API

- [OpenAI Chat Completions API](https://docs.openclaw.ai/gateway/openai-http-api)
- [OpenResponses HTTP API](https://docs.openclaw.ai/gateway/openresponses-http-api)

适合：

- 想用 `POST /v1/chat/completions`
- 想用 `POST /v1/responses`
- 想看 SSE 流式输出能力

---

## 5. 配置与运行

- [Gateway Configuration](https://docs.openclaw.ai/gateway/configuration)
- [Gateway Configuration Reference](https://docs.openclaw.ai/gateway/configuration-reference)
- [Gateway Runbook / 启动运行](https://docs.openclaw.ai/gateway/index)

适合：

- 想知道 Gateway 怎么启动
- 想确认 `gateway.auth.token`
- 想确认 `hooks.token`
- 想确认 `cron.webhookToken`

---

## 6. 这几篇文档怎么选

如果你现在的问题是：

- “OpenClaw 整体怎么设计的”
  看 [Architecture](https://docs.openclaw.ai/concepts/architecture)

- “`https://docs.openclaw.ai/automation/hooks` 是不是官方 hooks 页面”
  看 [Hooks 官方页面](https://docs.openclaw.ai/automation/hooks)

- “`webhooks` 插件该去哪里看”
  看 [Webhooks 插件官方页面](https://docs.openclaw.ai/plugins/webhooks)

- “`POST /v1/chat/completions` 怎么开启”
  看 [OpenAI Chat Completions API](https://docs.openclaw.ai/gateway/openai-http-api)

- “Spring Boot 里的 token 对应 OpenClaw 哪个配置项”
  看 [Gateway Configuration Reference](https://docs.openclaw.ai/gateway/configuration-reference)

---

## 7. 建议和项目内文档一起联读

建议配合项目内文档一起看：

- `openclaw-team-knowledge-base.md`
- `openclaw-terminology-guide.md`
- `openclaw-config-guide.md`
- `webhooks-taskflow-guide.md`

这样更适合把“官方文档的定义”和“本项目里的实际落地方式”对应起来。
