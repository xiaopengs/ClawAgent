# 爪爪 · AI Agent 对话

One Android client for talking to several agents. The language below names the
conversation domain; UI copy may use friendlier Chinese labels.

## Language

**Agent**:
A named assistant with an avatar, a description, and one conversation.
_Avoid_: bot, model (the model is behind a reply source, not the agent)

**Conversation**:
One agent's ordered message history together with its current reply phase.
_Avoid_: chat (reserve "chat" for the screen), session, thread

**Turn**:
One user message and the reply it triggers.
_Avoid_: round, exchange

**Reply**:
The assistant message produced for a turn. It may arrive in chunks and becomes
one message when settled.
_Avoid_: answer, completion (both are transport or model vocabulary)

**Reply source**:
The seam that turns an agent and a conversation history into a stream of reply
chunks. Production uses OpenRouter; tests use an in-memory source.
_Avoid_: backend, API, model client

**Waiting**:
The reply phase after a user turn is accepted but before the first chunk.
_Avoid_: typing (the UI shows a typing indicator, but the domain phase is waiting)

**Streaming**:
The reply phase after the first chunk has arrived and before the reply settles.
_Avoid_: loading, generating
