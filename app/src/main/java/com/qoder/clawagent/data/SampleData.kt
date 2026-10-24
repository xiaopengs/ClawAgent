package com.qoder.clawagent.data

import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AvatarStyle
import com.qoder.clawagent.model.ChatMessage
import com.qoder.clawagent.model.ExtensionOption
import com.qoder.clawagent.model.McpIconKind
import com.qoder.clawagent.model.McpService
import com.qoder.clawagent.model.PluginItem
import com.qoder.clawagent.model.Project
import com.qoder.clawagent.model.Skill
import com.qoder.clawagent.model.SkillSection
import com.qoder.clawagent.model.Template

object SampleData {

    fun seedAgents() = listOf(
        Agent(
            id = "heizhua",
            name = "黑爪爪",
            avatar = AvatarStyle.CAT,
            subtitle = "AI哨兵晚报（10/23）OpenAI 全面开放 Deci…",
            timeLabel = "11小时前",
            description = "每天早上整理 AI 行业值得关注的变化。",
        ),
        Agent(
            id = "coder",
            name = "编程专家",
            avatar = AvatarStyle.TERMINAL,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "34天前",
            description = "需求分析、代码实现、调试与评审。",
        ),
        Agent(
            id = "director",
            name = "视频导演",
            avatar = AvatarStyle.SUNGLASSES,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "34天前",
            description = "镜头语言、分镜脚本、视觉风格。",
        ),
        Agent(
            id = "claude",
            name = "云上Claude Code顾问",
            avatar = AvatarStyle.ROBOT,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "82天前",
            description = "在云电脑里持续运行 Claude Code、Codex CLI。",
        ),
        Agent(
            id = "thought",
            name = "思维实验",
            avatar = AvatarStyle.FACE,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "89天前",
            description = "把假设推演到底。",
        ),
        Agent(
            id = "designer",
            name = "产品设计师",
            avatar = AvatarStyle.SPARK,
            avatarColor = 0xFFD9E4F2,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "102天前",
            description = "用户路径、信息架构与界面细节。",
        ),
        Agent(
            id = "analyst",
            name = "数据分析师",
            avatar = AvatarStyle.GLASSES,
            avatarColor = 0xFFE7DCC8,
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "120天前",
            description = "数据清洗、指标口径与分析可视化。",
        ),
    )

    fun seedProjects() = listOf(
        Project(
            id = "inspiration-site",
            name = "灵感池网站",
            subtitle = "扣子编程：预览页面已完全重构，参考 Linear…",
            timeLabel = "83天前",
            isWebsite = true,
        ),
        Project(
            id = "inspiration",
            name = "灵感池",
            subtitle = "暂无消息，快去聊聊吧",
            timeLabel = "78天前",
            isWebsite = false,
        ),
    )

    fun initialMessages(agent: Agent): List<ChatMessage> = when (agent.id) {
        "heizhua" -> listOf(
            ChatMessage(
                id = 1,
                fromUser = false,
                title = "AI哨兵晚报（10/23）",
                text = "OpenAI 全面开放 Decisions API：基于 GPT-6 Luna，面向限定答案的准实时决策任务，响应速度比经 Responses API 调用最高提升 10 倍（部分场景从 1-3 秒降至 100-180 毫秒），输入每百万 token 0.10 美元、输出免费、单次最多提交 400 问——决策范式被“商品化”：当延迟进入亚毫秒、成本降到输出免费，实时决策从少数顶级实验室的专属能力，变成任何开发者都能随手调用的基础组件。\n\n对应用层来说，这意味着“一次调用完成一次判断”会成为默认设计：路由、风控、内容审核、客服分流都不再需要长链路推理。越早上车，越容易在体验和成本上拉开差距。",
                time = "20:02",
                processed = "已处理 46秒",
                linkTitle = "打开话题追踪工作台",
                linkSubtitle = "话题追踪 · 点击查看",
            ),
        )

        "coder" -> listOf(
            ChatMessage(
                id = 1,
                fromUser = false,
                text = "你好，我是编程专家。把需求、报错或仓库地址发给我，我可以直接开始定位问题、写代码，也可以帮你把修改跑通。",
                time = "09:12",
                processed = "已处理 1秒",
            ),
        )

        "director" -> listOf(
            ChatMessage(
                id = 1,
                fromUser = false,
                text = "我是视频导演。分镜、脚本、镜头节奏、视觉风格都可以聊，先告诉我你想做什么片子？",
                time = "11:30",
                processed = "已处理 1秒",
            ),
        )

        "claude" -> listOf(
            ChatMessage(
                id = 1,
                fromUser = false,
                text = "你好，我可以在云电脑里持续运行 Claude Code、Codex CLI 等工具。说说你想完成的项目，我来安排执行。",
                time = "08:45",
                processed = "已处理 1秒",
            ),
        )

        "thought" -> listOf(
            ChatMessage(
                id = 1,
                fromUser = false,
                text = "来做个思维实验吧。你可以抛给我一个假设，我们从不同的角度把它推演到底。",
                time = "10:20",
                processed = "已处理 1秒",
            ),
        )

        else -> listOf(
            ChatMessage(
                id = 1,
                fromUser = false,
                text = "你好，我是${agent.name}。${agent.description.ifBlank { "告诉我你想完成什么，我会在这里持续跟进。" }}",
                time = "09:00",
                processed = "已处理 1秒",
            ),
        )
    }

    val skillSections = listOf(
        SkillSection(
            title = "自媒体",
            skills = listOf(
                Skill(
                    id = "skill-xhs-query",
                    name = "小红书爆款笔记查询",
                    description = "小红书爆款笔记洞察技能，追踪热点选题与爆款结构。",
                    tag = "限时免费",
                    icon = "🦊",
                    iconBg = 0xFFFCF0E2,
                ),
                Skill(
                    id = "skill-wechat-10w",
                    name = "公众号10w+文章",
                    description = "精准锚定 10w+ 爆款文章，拆解标题与内容框架。",
                    tag = "限时免费",
                    icon = "10w+",
                    iconBg = 0xFFF6EFD8,
                ),
                Skill(
                    id = "skill-wechat-hot",
                    name = "公众号热门文章查询",
                    description = "搜索公众号热门文章，专为选题与内容策划设计。",
                    tag = "限时免费",
                    icon = "🔍",
                    iconBg = 0xFFEDF4E6,
                ),
                Skill(
                    id = "skill-xhs-diagnose",
                    name = "小红书账号诊断",
                    description = "从封面、标题到互动数据，给账号做一次体检。",
                    tag = "限时免费",
                    icon = "🩺",
                    iconBg = 0xFFFCEAEA,
                ),
            ),
        ),
        SkillSection(
            title = "金融",
            skills = listOf(
                Skill(
                    id = "skill-a-share",
                    name = "A股投资分析技能包",
                    description = "基于深度量化策略与多维数据，生成结构化投资分析。",
                    tag = "技能包 | 含付费",
                    icon = "📈",
                    iconBg = 0xFFF9E1E4,
                    footer = "包含 9 个技能",
                ),
                Skill(
                    id = "skill-report",
                    name = "财报解读助手",
                    description = "一键拆解三大报表，定位关键指标与风险点。",
                    tag = "含付费",
                    icon = "📊",
                    iconBg = 0xFFE8EFFC,
                ),
            ),
        ),
        SkillSection(
            title = "法律",
            skills = listOf(
                Skill(
                    id = "skill-contract",
                    name = "合同审阅助手",
                    description = "识别常见合同风险条款，给出修改建议。",
                    tag = "限时免费",
                    icon = "⚖️",
                    iconBg = 0xFFE9EDF5,
                ),
                Skill(
                    id = "skill-law-search",
                    name = "法律检索",
                    description = "快速定位法规与案例，附带来源。",
                    tag = "限时免费",
                    icon = "📚",
                    iconBg = 0xFFF4EEE3,
                ),
            ),
        ),
        SkillSection(
            title = "互联网",
            skills = listOf(
                Skill(
                    id = "skill-competitor",
                    name = "竞品雷达",
                    description = "跟踪竞品更新、定价与用户反馈。",
                    tag = "限时免费",
                    icon = "📡",
                    iconBg = 0xFFE8F2F0,
                ),
                Skill(
                    id = "skill-feedback",
                    name = "用户反馈聚类",
                    description = "把零散反馈归纳成可执行的产品主题。",
                    tag = "限时免费",
                    icon = "💬",
                    iconBg = 0xFFEDEBFB,
                ),
            ),
        ),
    )

    val mcpServices = listOf(
        McpService(
            id = "mcp-qcc",
            name = "企查查·企业数据",
            description = "提供企业工商、股东、主体资质和基础经营信息查询…",
            icon = McpIconKind.QCC,
        ),
        McpService(
            id = "mcp-tyc",
            name = "天眼查",
            description = "提供企业主体识别、工商画像、集团关系、风险合规…",
            icon = McpIconKind.TYC,
        ),
        McpService(
            id = "mcp-agentkey",
            name = "AgentKey",
            description = "通过统一远程入口为智能体路由搜索、网页、社交…",
            icon = McpIconKind.AGENTKEY,
        ),
        McpService(
            id = "mcp-notion",
            name = "Notion",
            description = "Notion 托管 MCP 服务，用于搜索、读取和维护工作…",
            icon = McpIconKind.NOTION,
        ),
        McpService(
            id = "mcp-github",
            name = "GitHub",
            description = "GitHub MCP 服务，为智能助手开放仓库、议题、拉…",
            icon = McpIconKind.GITHUB,
        ),
        McpService(
            id = "mcp-ifind",
            name = "同花顺 iFinD·A股",
            description = "提供A股选股、行情、公司资料、财务、股东、风险…",
            icon = McpIconKind.IFIND,
        ),
        McpService(
            id = "mcp-octopus",
            name = "八爪鱼",
            description = "八爪鱼中国区 MCP 服务，用于搜索采集模板、创建…",
            icon = McpIconKind.OCTOPUS,
        ),
    )

    val plugins = listOf(
        PluginItem(
            id = "plugin-web",
            name = "联网搜索",
            description = "搜索公开网页，返回带来源的最新信息。",
            icon = "🌐",
            iconBg = 0xFFE7F0FB,
        ),
        PluginItem(
            id = "plugin-image",
            name = "图像生成",
            description = "根据描述生成图片，支持尺寸与风格控制。",
            icon = "🎨",
            iconBg = 0xFFF7E8F3,
        ),
        PluginItem(
            id = "plugin-code",
            name = "代码解释器",
            description = "在隔离环境中运行 Python，分析并可视化数据。",
            icon = "🧪",
            iconBg = 0xFFEAF2E3,
        ),
        PluginItem(
            id = "plugin-doc",
            name = "文档阅读",
            description = "读取 PDF、Word、PPT 等文件并提取要点。",
            icon = "📄",
            iconBg = 0xFFF7EEDF,
        ),
    )

    val templates = listOf(
        Template(
            id = "template-director",
            name = "视频导演",
            icon = "🕶️",
            iconBg = 0xFFCBB9F6,
            description = "镜头语言、分镜脚本、视觉风格",
        ),
        Template(
            id = "template-coder",
            name = "编程专家",
            icon = ">_",
            iconBg = 0xFFCBDCC6,
            description = "需求分析、代码实现、调试与评审",
        ),
        Template(
            id = "template-invest",
            name = "投资顾问",
            icon = "👓",
            iconBg = 0xFFAFBCF0,
            description = "组合分析、估值框架、风险提示",
        ),
        Template(
            id = "template-media",
            name = "自媒体运营达人",
            icon = "💻",
            iconBg = 0xFFF3C2D2,
            description = "选题策划、内容拆解、账号增长",
        ),
        Template(
            id = "template-legal",
            name = "金牌法务",
            icon = "⚖️",
            iconBg = 0xFFD3D3DC,
            description = "合同审阅、合规建议、风险清单",
        ),
    )

    val extensionOptions = listOf(
        ExtensionOption("ext-web", "联网搜索", "插件"),
        ExtensionOption("ext-image", "图像生成", "插件"),
        ExtensionOption("ext-code", "代码解释器", "插件"),
        ExtensionOption("ext-xhs", "小红书爆款笔记查询", "技能"),
        ExtensionOption("ext-notion", "Notion", "MCP"),
        ExtensionOption("ext-github", "GitHub", "MCP"),
    )

    val agentNameIdeas = listOf("星野", "小满", "山岚", "望舒", "拾光", "昭野", "青梧", "知夏")
}
