package com.qoder.clawagent.chat

import com.qoder.clawagent.model.Agent

/**
 * The persona every backend shares. Keeping it here means the OpenRouter
 * adapter and the ADK runtime describe the same agent to the model.
 */
internal fun agentInstruction(agent: Agent): String = buildString {
    append("你是「").append(agent.name).append("」，")
    append(agent.description.ifBlank { "一位可靠的中文 AI 助手" })
    append("。请始终使用简体中文回答，语气自然、简洁、直接；")
    append("不要编造不确定的信息，不知道时就明确说明。")
}
