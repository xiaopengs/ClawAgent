# v1.4 界面功能点核对

核对范围：参考稿中的对话首页、Agent 对话、扩展广场（插件 / 技能 / MCP）、新建 Agent 流程，以及 v1.4 补齐的项目、日程、云盘、个人中心、套餐、话题、Agent 设置、三方 Agent 与本地 Agent 页面。

状态说明：

- **已实现**：有完整交互与状态流转，不只是静态布局。
- **演示**：交互完整，但依赖本地状态或外部服务，界面中已明确标注。
- **需外部服务**：客户端已提供配置与入口，真实执行需要云端 worker。

## 对话首页与项目

| 功能点 | 状态 | 实现与验证 |
| --- | --- | --- |
| Agent 列表、最近项目、时间与预览摘要 | 已实现 | `HomeScreen.kt`；`home.png` |
| Agent 搜索过滤 | 已实现 | 顶部搜索按钮展开输入框，按名称 / 摘要过滤 |
| 列表展开 / 收起 | 已实现 | 「展开」切换可见 Agent 数 |
| 右上角「+」菜单 | 已实现 | 新建 Agent、新建项目入口 |
| 新建通用 / 网站项目 | 已实现 | 项目名与类型，创建后进入项目详情；`AppViewModelTest` 覆盖 |
| 项目详情、工作目录、最近活跃 | 已实现 | `ProjectDetailScreen.kt`；`project-detail.png` |
| 项目对话 | 已实现 | 每个项目复用一条隐藏 `ChatConversation`，不出现在 Agent 列表 |
| 个人进阶入口 | 已实现 | 顶部胶囊与底部「更多」均可达 |

## Agent 对话

| 功能点 | 状态 | 实现与验证 |
| --- | --- | --- |
| 流式回复、首包上屏、停止 / 重试 / 清空 | 已实现 | `ChatConversation`；9 个领域测试 |
| 401 / 402 / 429 / 超时 / 断网中文提示 | 已实现 | `ChatFailures.kt` 与错误卡片 |
| 长文「显示更多 / 收起」 | 已实现 | `ChatThread.kt`；`chat.png` |
| 用户消息、附件消息、话题追踪卡 | 已实现 | 图片 240×160 裁切；文件芯片显示名称 / 大小 |
| 顶部运行时状态（云端 / 云电脑 / 本地） | 已实现 | `ChatHeader.kt` + `WorkspaceDialog` |
| 工作区详情（模型、引擎、目录、端点、扩展） | 已实现 | `ChatDialogs.kt` |
| 「已处理 N 秒」详情（耗时、模型、输出字数、附件数） | 已实现 | `ProcessedDialog` |
| 图片多选（最多 4 张）与文件选择（总附件上限 6） | 已实现 | `PickMultipleVisualMedia` / `OpenDocument`；图片缩放 1280px、JPEG 82% |
| 文本文件读取（前 16 KB）与二进制元数据 | 已实现 | `Attachments.kt` |
| 附件进入 OpenRouter / ADK / 本地三种运行时 | 已实现 | data URL 图片与文本拼装；`OpenRouterTest` 覆盖 |
| 语音输入（填入输入框） | 已实现 | `SpeechRecognizer`；运行时权限；单次收音 |
| 旁听（持续收音，结束后发送） | 已实现 | `VoiceCaptureDialog` 的 `LISTEN` 模式；支持重新收音 |
| 无语音服务 / 权限拒绝处理 | 已实现 | 中文提示，不崩溃 |
| 插件 / 技能入口 | 演示 | 打开扩展面板，未接真实执行后端 |
| 模型设置（Key、模型、Base URL、引擎） | 已实现 | `OpenRouterSettingsDialog`；保存后立即生效 |

## 扩展广场与新建 Agent

| 功能点 | 状态 | 实现与验证 |
| --- | --- | --- |
| 插件 / 技能 / MCP 三个 Tab | 已实现 | `ExtensionsOverlay.kt`；`plugins.png`、`skills.png`、`mcp.png` |
| 技能分类筛选、添加 / 已添加 | 已实现 | 本地状态流转 |
| 新建 Agent 三条路径 | 已实现 | 扣子 Agent、三方精选、本地 Agent；`new-agent.png` |
| 扣子职业模板、直接创建 | 已实现 | `CozeTemplatesScreen`；`templates.png` |
| 名称一键换名、工作目录同步 | 已实现 | `AgentFormScreen.kt`；`form.png` |
| 描述、扩展能力多选 | 已实现 | 扩展选择弹窗与标签展示 |
| 创建后回到首页并置顶 | 已实现 | `AppViewModel.createAgent` |

## Agent 设置与三方 / 本地 Agent

| 功能点 | 状态 | 实现与验证 |
| --- | --- | --- |
| 编辑名称、工作目录、描述 | 已实现 | `AgentSettingsScreen.kt`；`agent-settings.png` |
| 保存后同步当前会话 | 已实现 | `ChatConversation.updateAgent`；`AppViewModelTest` 覆盖 |
| 自定义 Agent 删除 + 二次确认 | 已实现 | 种子 Agent 受保护，不可删除 |
| 三方 Agent 目录与配置 | 演示 | Claude Code / Codex / Gemini / OpenCode；`third-party.png` |
| 三方 CLI 真实执行 | 需外部服务 | 页面明确标注「真实 CLI 执行需要连接 worker」 |
| 本地 Agent 端点 / 模型 / 可选 Key | 已实现 | `LocalAgentScreen.kt`；`local-agent.png` |
| 连接测试 `GET /models` 与模型选择 | 已实现 | `LocalEndpointProbe`；失败有中文提示 |
| 本地 Agent 对话 | 已实现 | `LocalReplySource` 直连 OpenAI 兼容 `/v1` |

## 日程、云盘、话题与个人中心

| 功能点 | 状态 | 实现与验证 |
| --- | --- | --- |
| 新增日程（标题、时间、重复、执行 Agent） | 已实现 | `ScheduleScreen.kt`；`schedule.png` |
| 完成 / 取消完成、删除、空状态 | 已实现 | 本地状态；`AppViewModelTest` 覆盖 |
| 日程真正到点执行 | 需外部服务 | 页面标注「定时执行需接入云端 worker」 |
| 云盘添加 / 大小 / 类型 / 时间 | 已实现 | 系统文件选择器；`drive.png` |
| 文本预览、系统应用打开、删除、空状态 | 已实现 | 无本地 URI 的演示文件提供文本预览 |
| 话题新增 / 关注开关 / 删除 | 已实现 | `TopicTrackingScreen.kt`；`topics.png` |
| 对话中的话题追踪卡跳转 | 已实现 | 卡片 → 话题工作台 |
| 个人中心账号卡、统计、入口 | 已实现 | `ProfileScreen.kt`；`profile.png` |
| 通知开关、帮助 / 关于、版本号 | 演示 | 本地状态与对话框 |
| Free / Pro 对比与演示升级 | 演示 | 明确提示「不产生真实扣费」；`upgrade.png` |

## 验证结果

```text
./gradlew :app:assembleDebug      BUILD SUCCESSFUL
./gradlew :app:lintDebug          BUILD SUCCESSFUL（0 error）
./gradlew :app:testDebugUnitTest  BUILD SUCCESSFUL（41 tests, 0 failed, 2 skipped live tests）
./gradlew :app:recordRoborazziDebug  17 screenshots
```

- 确定性测试 39 个：`ChatConversationTest` 9、`OpenRouterRepositoryTest` 5、`AppViewModelTest` 6、`AdkReplySourceTest` 2、`ScreenshotTest` 17。
- 可选联调 2 个：`OpenRouterLiveTest`、`AdkLiveTest`，默认跳过，需 `OPENROUTER_LIVE_TEST=1`。
- 截图：`docs/screenshots/`，共 17 张，1080×2340。

## 边界与后续

1. 三方 CLI、定时调度、真实支付、登录 / 账号服务需要服务端；当前客户端不伪装这些能力。
2. 插件、技能、MCP、通知、帮助 / 关于为本地交互演示。
3. 本地 Agent 的 API Key 仅保存在进程内存；正式产品应改为后端短期凭证。
4. 云端模型 Key 仍可能进入 debug APK 的 `BuildConfig`，交付包已使用空 Key 重新构建。
