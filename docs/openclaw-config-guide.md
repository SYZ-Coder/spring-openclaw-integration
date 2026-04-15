# OpenClaw 配置与官方文档对照指南

## 1. 文档目标

这份文档专门说明两件事：

- Spring Boot 示例项目中的 `openclaw.*` 配置项，分别对应 OpenClaw 官方配置里的什么位置
- 如何在 OpenClaw 中开启 Gateway 的 OpenAI 兼容端点，尤其是 `POST /v1/chat/completions`

---

## 1.1 官方文档对应关系表

下面这张表适合在“我现在看到一个概念，但不知道该去 OpenClaw 官方哪篇文档看”时快速查阅。

| 你要看的内容 | 官方文档地址 | 说明 |
|---|---|---|
| Hooks 机制 | `https://docs.openclaw.ai/automation/hooks` | 主要讲 internal hooks、plugin hooks，以及 hooks 相关自动化机制 |
| `webhooks` 插件 | `https://docs.openclaw.ai/plugins/webhooks` | 主要讲插件 route、认证方式，以及如何把外部请求桥接到 TaskFlow |
| Gateway OpenAI Chat Completions | `https://docs.openclaw.ai/gateway/openai-http-api` | 主要讲 `POST /v1/chat/completions`、鉴权和兼容行为 |
| Gateway OpenResponses API | `https://docs.openclaw.ai/gateway/openresponses-http-api` | 主要讲 `POST /v1/responses` 和 SSE 流式能力 |
| Gateway 配置 | `https://docs.openclaw.ai/gateway/configuration` | 适合先看整体配置思路和常见配置示例 |
| Gateway 配置参考 | `https://docs.openclaw.ai/gateway/configuration-reference` | 适合精确查 `gateway.auth.token`、`cron.webhookToken`、`hooks.token` 等字段 |
| Gateway 启动与运行 | `https://docs.openclaw.ai/gateway/index` | 适合查看 Gateway 启动、状态检查和运行方式 |

这里特别说明一下容易混淆的一组：

- `https://docs.openclaw.ai/automation/hooks` 是官方 hooks 页面
- 它主要对应 hooks 机制，不是 `webhooks` 插件专项文档
- 如果你要看 `webhooks` 插件，请去 `https://docs.openclaw.ai/plugins/webhooks`

---

## 2. 先区分两类配置

在示例项目里，`application.yml` 中有这样一段：

```yaml
openclaw:
  base-url: http://127.0.0.1:18789
  token: YOUR_GATEWAY_TOKEN
  callback-token: MY_CRON_WEBHOOK_TOKEN
```

这一段不是 OpenClaw 官方原生的 `openclaw.json` 配置结构。

它是 Spring Boot 示例项目为了调用 OpenClaw Gateway 而定义的“客户端配置”。

也就是说：

- `application.yml` 里的 `openclaw.*` 是我们 Java 项目自己的配置
- OpenClaw 服务端真正读取的是它自己的 Gateway 配置文件和环境变量

---

## 3. 配置对应关系

| Spring Boot 示例配置 | 作用 | OpenClaw 官方配置对应项 | 说明 |
|---|---|---|---|
| `openclaw.base-url` | 指向 Gateway 地址 | `gateway.bind` + `gateway.port` | 组合起来决定 OpenClaw 实际监听地址 |
| `openclaw.token` | 调用 `/v1/*` 时使用的 Bearer Token | `gateway.auth.mode = "token"` + `gateway.auth.token` | Gateway HTTP API 的访问鉴权 |
| `openclaw.callback-token` | 校验 OpenClaw 主动回调时的 Bearer Token | `cron.webhookToken` | OpenClaw 通过 cron webhook 回调外部系统时使用 |
| 不在 Spring Boot 中，而是外部调用 `/hooks/*` 用的 token | 外部系统触发 hooks | `hooks.token` | 用于 `/hooks/wake`、`/hooks/agent` 等入口 |

---

## 4. OpenClaw 里分别在哪里配置

### 4.1 Gateway 地址

如果你在 Spring Boot 里写的是：

```yaml
openclaw:
  base-url: http://127.0.0.1:18789
```

那在 OpenClaw 侧通常对应的是：

```json5
{
  gateway: {
    bind: "loopback",
    port: 18789
  }
}
```

这里并不存在一个叫 `base-url` 的 OpenClaw 原生字段。`base-url` 只是客户端为了方便而拼出来的调用地址。

### 4.2 Gateway Token

如果你的 Spring Boot 里写的是：

```yaml
openclaw:
  token: YOUR_GATEWAY_TOKEN
```

那 OpenClaw 侧对应的是：

```json5
{
  gateway: {
    auth: {
      mode: "token",
      token: "YOUR_GATEWAY_TOKEN"
    }
  }
}
```

### 4.3 callback-token

如果你的 Spring Boot 回调接收端写的是：

```yaml
openclaw:
  callback-token: MY_CRON_WEBHOOK_TOKEN
```

那在 OpenClaw 侧对应的是：

```json5
{
  cron: {
    webhookToken: "MY_CRON_WEBHOOK_TOKEN"
  }
}
```

当 OpenClaw 的 cron 任务以 `delivery.mode = "webhook"` 回调外部系统时，会带上：

```http
Authorization: Bearer MY_CRON_WEBHOOK_TOKEN
```

### 4.4 hooks token

如果你要让外部系统主动调用：

- `POST /hooks/wake`
- `POST /hooks/agent`

那需要配置：

```json5
{
  hooks: {
    enabled: true,
    token: "MY_HOOK_TOKEN",
    path: "/hooks"
  }
}
```

这里要特别注意：

- `hooks.token` 是外部系统“请求 OpenClaw”时用
- `cron.webhookToken` 是 OpenClaw“回调外部系统”时用

---

## 5. 如何开启 `POST /v1/chat/completions`

OpenClaw Gateway 可以提供一个小型的 OpenAI 兼容 Chat Completions 端点，但这个端点默认是关闭的。

要启用它，需要在 OpenClaw 配置里显式打开：

```json5
{
  gateway: {
    auth: {
      mode: "token",
      token: "YOUR_GATEWAY_TOKEN"
    },
    http: {
      endpoints: {
        chatCompletions: {
          enabled: true
        }
      }
    }
  }
}
```

启用后即可请求：

```http
POST /v1/chat/completions
```

如果你还想同时启用更推荐的 `/v1/responses`，可以一起配：

```json5
{
  gateway: {
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
  }
}
```

---

## 6. 最小可运行的 OpenClaw 配置示例

下面这份适合“Spring Boot 调用 + webhook 回调 + hooks 触发 + Chat Completions”四种场景一起准备好：

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

---

## 7. Gateway 如何启动

按官方 Gateway Runbook 的思路，本地最小启动通常类似：

```bash
openclaw gateway --port 18789
```

常见辅助命令：

```bash
openclaw gateway --port 18789 --verbose
openclaw gateway status
openclaw status
openclaw logs --follow
```

启动后，你可以先检查模型列表：

```bash
curl http://127.0.0.1:18789/v1/models \
  -H "Authorization: Bearer YOUR_GATEWAY_TOKEN"
```

---

## 8. 官方文档位置

下面是这部分配置最重要的官方文档入口：

- Gateway 总入口：
  [https://docs.openclaw.ai/gateway/index](https://docs.openclaw.ai/gateway/index)
- Gateway 配置说明：
  [https://docs.openclaw.ai/gateway/configuration](https://docs.openclaw.ai/gateway/configuration)
- 配置项参考：
  [https://docs.openclaw.ai/gateway/configuration-reference](https://docs.openclaw.ai/gateway/configuration-reference)
- OpenAI Chat Completions 兼容接口：
  [https://docs.openclaw.ai/gateway/openai-http-api](https://docs.openclaw.ai/gateway/openai-http-api)
- OpenResponses HTTP API：
  [https://docs.openclaw.ai/gateway/openresponses-http-api](https://docs.openclaw.ai/gateway/openresponses-http-api)

建议团队阅读顺序：

1. 先看 Gateway 总入口，理解 OpenClaw Gateway 的角色
2. 再看 OpenAI Chat Completions 和 OpenResponses 两个 HTTP 接口文档
3. 最后看 Configuration 与 Configuration Reference，定位具体配置项

---

## 9. 团队实践建议

建议在团队内部统一采用下面的记忆方式：

- Java 项目里的 `openclaw.*` 配置，理解成“调用方配置”
- OpenClaw 的 `gateway.* / cron.* / hooks.*` 配置，理解成“服务端配置”

如果后续团队要继续扩展，可以再补两类文档：

- `openclaw.json` 分环境模板
- 生产环境部署与反向代理配置模板
