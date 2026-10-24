package com.qoder.clawagent.chat

/**
 * Which runtime answers a conversation turn.
 *
 * Both engines sit behind [ReplySource], so switching does not touch the UI
 * or the conversation state machine.
 */
enum class ReplyEngine {
    /** Direct OpenAI-compatible streaming call; the lightest path. */
    OPENROUTER,

    /** Google ADK runtime with sessions, instructions and tool orchestration. */
    ADK,
}
