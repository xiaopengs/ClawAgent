# Conversation behavior lives in ChatConversation

Status: accepted

Chat behavior (streaming, retry, clear, elapsed time, failure messages) used to
be interleaved with navigation and agent creation in `AppViewModel`, and the
message list lived in Compose snapshot state. That made the behavior hard to
test without Android and forced every chat change through one large class. We
moved it behind `ChatConversation`, which exposes `state`, `send`, `retry` and
`clear`, and takes a `ReplySource` seam. Production uses `OpenRouterRepository`;
tests use an in-memory source that emits or fails on demand. `AppViewModel` now
only orchestrates navigation, agents and previews, and `ChatScreen` collects
`StateFlow<ChatState>`.

Consequences: reply transports implement `ReplySource`; conversation tests
cross the same interface as the UI and cover streaming, retry, cancellation and
failure mapping. `ChatScreen` was split into header, thread and settings
modules so each file changes for one reason.
