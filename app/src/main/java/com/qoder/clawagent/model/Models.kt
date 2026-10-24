package com.qoder.clawagent.model

enum class AvatarStyle {
    CAT,
    TERMINAL,
    SUNGLASSES,
    ROBOT,
    FACE,
    SPARK,
    GLASSES,
}

data class Agent(
    val id: String,
    val name: String,
    val avatar: AvatarStyle,
    val avatarColor: Long? = null,
    val subtitle: String,
    val timeLabel: String,
    val description: String = "",
)

data class Project(
    val id: String,
    val name: String,
    val subtitle: String,
    val timeLabel: String,
    val isWebsite: Boolean,
)

data class ChatMessage(
    val id: Long,
    val fromUser: Boolean,
    val text: String,
    val title: String? = null,
    val time: String = "",
    val processed: String? = null,
    val linkTitle: String? = null,
    val linkSubtitle: String? = null,
    val isError: Boolean = false,
)

enum class McpIconKind {
    QCC,
    TYC,
    AGENTKEY,
    NOTION,
    GITHUB,
    IFIND,
    OCTOPUS,
}

data class McpService(
    val id: String,
    val name: String,
    val description: String,
    val icon: McpIconKind,
)

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val tag: String,
    val icon: String,
    val iconBg: Long,
    val highlight: Boolean = false,
    val footer: String? = null,
)

data class SkillSection(
    val title: String,
    val skills: List<Skill>,
)

data class PluginItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val iconBg: Long,
)

data class Template(
    val id: String,
    val name: String,
    val icon: String,
    val iconBg: Long,
    val description: String,
)

data class ExtensionOption(
    val id: String,
    val name: String,
    val group: String,
)

sealed class AppScreen {
    data object Home : AppScreen()
    data class Chat(val agentId: String) : AppScreen()
    data object NewAgent : AppScreen()
    data object CozeTemplates : AppScreen()
    data class AgentForm(val templateId: String?) : AppScreen()
    data class Tab(val title: String) : AppScreen()
}
