package com.qoder.clawagent.chat

/**
 * Turns transport/model failures into something the person in the chat can act on.
 * Kept as a pure function so the mapping is easy to test and change in one place.
 */
internal fun describeReplyFailure(error: Throwable): String {
    if (error is ReplyUnavailableException) {
        return error.message.orEmpty()
    }

    val details = buildString {
        append(error.message.orEmpty())
        var cause = error.cause
        var depth = 0
        while (cause != null && depth < 3) {
            append(" ").append(cause.message.orEmpty())
            cause = cause.cause
            depth += 1
        }
    }

    return when {
        details.contains("401") || details.contains("unauthorized", ignoreCase = true) ->
            "API Key 无效或已过期。请点击右上角 ··· →「模型设置」更新 Key。"
        details.contains("402") ->
            "OpenRouter 账户余额不足，当前免费额度可能已用完。"
        details.contains("429") ->
            "请求过于频繁，或今日免费模型额度已用尽。请稍后再试或更换模型。"
        details.contains("timeout", ignoreCase = true) ->
            "请求超时。请检查网络后重试。"
        details.contains("Unable to resolve host", ignoreCase = true) ||
            details.contains("connect", ignoreCase = true) ||
            details.contains("network", ignoreCase = true) ->
            "网络连接失败。请检查网络后重试。"
        else -> "OpenRouter 请求失败：${
            details.ifBlank { error::class.simpleName ?: "未知错误" }.take(140)
        }"
    }
}
