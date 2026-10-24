package com.qoder.clawagent.chat

import com.qoder.clawagent.model.Agent
import com.qoder.clawagent.model.AgentKind

/**
 * The persona every backend shares. Keeping it here means the OpenRouter
 * adapter, local endpoint client and ADK runtime describe the same agent.
 */
internal fun agentInstruction(agent: Agent): String = buildString {
    append("你是「").append(agent.name).append("」，")
    append(agent.description.ifBlank { "一位可靠的中文 AI 助手" })
    append("。请始终使用简体中文回答，语气自然、简洁、直接；")
    append("不要编造不确定的信息，不知道时就明确说明。")
    if (agent.workdir.isNotBlank()) {
        append("当前工作目录是「").append(agent.workdir).append("」。")
    }
    when (agent.kind) {
        AgentKind.THIRD_PARTY -> {
            append("你以云电脑终端 Agent 的方式协作")
            if (!agent.command.isNullOrBlank()) {
                append("（命令：").append(agent.command).append("）")
            }
            append("。可以给出命令、补丁与执行计划，但不要声称已经执行了实际未执行的操作。")
        }

        AgentKind.LOCAL -> {
            append("你由用户设备上的本地 OpenAI 兼容模型服务提供推理。")
            append("图片与文件内容会作为本轮上下文传入，请基于它们回答。")
        }

        AgentKind.COZE -> Unit
    }
}
