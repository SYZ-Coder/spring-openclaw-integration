# 文档导航

这个目录用于沉淀团队围绕 OpenClaw 与 Spring Boot 集成的学习资料、架构理解和落地经验。

## 推荐阅读顺序

1. `openclaw-team-knowledge-base.md`
2. `openclaw-study.md`
3. `openclaw-official-links.md`
4. `openclaw-terminology-guide.md`
5. `openclaw-config-guide.md`
6. `webhooks-taskflow-guide.md`
7. `team-learning-notes-2026-04-14.md`

## 文档说明

### `openclaw-team-knowledge-base.md`

这是团队正式版知识库文档，适合作为主入口。

内容包括：

- OpenClaw 的总体定位
- 架构分层
- hook / plugin / gateway 扩展机制
- 外部请求与外部回调方案
- Spring Boot 集成架构图与时序图
- Java WebClient + SSE 接入方案
- 术语表
- FAQ

适合人群：

- 新加入项目的开发同学
- 需要整体理解 OpenClaw 的后端同学
- 需要做长期维护和知识沉淀的团队成员

### `openclaw-study.md`

这是偏源码分析和架构学习的文档。

内容包括：

- OpenClaw 架构设计与功能解析
- 总体架构图
- Spring Boot 调 OpenClaw 的架构图与时序图
- hook、plugin、gateway、webhooks 插件分析
- 配置映射与官方文档入口

适合人群：

- 想快速理解 OpenClaw 内部设计思路的人
- 需要做技术分享、方案说明的人

### `openclaw-config-guide.md`

这是偏配置对照和落地接入的专项文档。

内容包括：

- Spring Boot 示例配置与 OpenClaw 原生配置的对应关系
- `gateway.auth.token`、`cron.webhookToken`、`hooks.token` 的区别
- 如何开启 `POST /v1/chat/completions`
- 最小可运行的 `openclaw.json` 示例
- 官方文档入口整理

适合人群：

- 需要把 Java 服务接入 OpenClaw 的后端开发
- 需要排查“这个 token 应该配在哪里”的同学
- 需要快速找到 OpenClaw 官方配置文档的人

### `openclaw-official-links.md`

这是偏官方文档导航和收藏入口的链接页。

内容包括：

- OpenClaw 官方文档首页
- hooks、`webhooks` 插件、HTTP API、配置、架构等常用官方地址
- 按问题场景推荐该看哪篇官方文档

适合人群：

- 需要快速跳转官方文档的人
- 做培训、分享、知识沉淀时需要统一引用入口的人
- 想收藏一页官方链接导航的团队成员

### `openclaw-terminology-guide.md`

这是偏术语统一和团队沟通规范的速查文档。

内容包括：

- `hook`、`hooks`、`webhook`、`webhooks` 插件的区别
- callback webhook、TaskFlow、cron delivery 的定位
- 三类能力分别由谁发起、适合什么场景
- 与本项目代码结构的对应关系

适合人群：

- 新加入项目、容易被名词绕晕的同学
- 需要做方案沟通、技术分享的人
- 想快速搞清楚术语边界的后端和平台同学

### `webhooks-taskflow-guide.md`

这是偏实现原理和项目落地的专项文档。

内容包括：

- `webhooks` 插件的工作原理
- TaskFlow 的职责与适用场景
- 两者之间的关系
- 本项目中新增加的 Spring Boot 示例代码说明
- `create_flow`、`list_flows`、`run_task` 的调用示例

适合人群：

- 需要对接 OpenClaw 工作流能力的后端开发
- 需要把外部系统和 OpenClaw 自动化打通的同学

### `team-learning-notes-2026-04-14.md`

这是本次交流内容的团队学习整理版。

内容包括：

- 本次问答核心结论
- Spring Boot 示例项目说明
- Java SSE 接入思路
- 项目输出内容
- 踩坑记录
- 团队实施建议

适合人群：

- 想了解这次知识沉淀背景的人
- 后续做二次整理或知识追溯的人

## 后续可以继续补充的文档

如果后续继续扩展，建议在 `docs` 目录下增加以下材料：

- 部署说明
- DTO / SDK 设计文档
- 回调安全规范
- 前端 SSE 接入说明
- FAQ 扩展

## 建议维护方式

- 把 `openclaw-team-knowledge-base.md` 当成团队主文档长期维护
- 把 `openclaw-terminology-guide.md` 当成团队统一术语速查页
- `openclaw-study.md` 作为架构分析文档保留
- `openclaw-config-guide.md` 作为配置和接入专项文档保留
- `webhooks-taskflow-guide.md` 作为自动化与工作流专项文档保留
- `team-learning-notes-2026-04-14.md` 作为历史学习记录保留
