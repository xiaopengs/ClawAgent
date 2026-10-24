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

enum class AgentKind {
    COZE,
    THIRD_PARTY,
    LOCAL,
}

data class Agent(
    val id: String,
    val name: String,
    val avatar: AvatarStyle,
    val avatarColor: Long? = null,
    val subtitle: String,
    val timeLabel: String,
    val description: String = "",
    val kind: AgentKind = AgentKind.COZE,
    val workdir: String = "",
    val extensions: List<String> = emptyList(),
    val localEndpoint: String? = null,
    val localModel: String? = null,
    val localApiKey: String? = null,
    val command: String? = null,
    /** Project agents still power a chat, but stay out of the Agent list. */
    val visibleInList: Boolean = true,
)

data class Project(
    val id: String,
    val name: String,
    val subtitle: String,
    val timeLabel: String,
    val isWebsite: Boolean,
)

enum class AttachmentKind {
    IMAGE,
    FILE,
}

/**
 * A local attachment snapshot. Images keep a data URL so the conversation can
 * be replayed to either engine without re-reading a content URI that may have
 * lost its permission.
 */
data class ChatAttachment(
    val id: String,
    val kind: AttachmentKind,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long = 0,
    val dataUrl: String? = null,
    val text: String? = null,
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
    val attachments: List<ChatAttachment> = emptyList(),
)

data class ScheduleTask(
    val id: String,
    val title: String,
    val timeLabel: String,
    val repeatLabel: String,
    val agentName: String,
    val done: Boolean = false,
)

data class DriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeLabel: String,
    val addedLabel: String,
    val uri: String? = null,
    val previewText: String? = null,
)

enum class Plan {
    FREE,
    PRO,
}

data class ThirdPartyAgent(
    val id: String,
    val name: String,
    val description: String,
    val command: String,
    val icon: String,
    val iconBg: Long,
    val avatar: AvatarStyle = AvatarStyle.SPARK,
)

data class TopicTrackingItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val updatedLabel: String,
    val followed: Boolean = true,
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
    data class ProjectDetail(val projectId: String) : AppScreen()
    data class AgentSettings(val agentId: String) : AppScreen()
    data class TopicTracking(val topicId: String? = null) : AppScreen()
    data object ThirdPartyAgents : AppScreen()
    data object LocalAgent : AppScreen()
    data object Profile : AppScreen()
    data object Upgrade : AppScreen()
}
