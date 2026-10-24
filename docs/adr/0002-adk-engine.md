# ADK Kotlin runs behind the ReplySource seam

Status: accepted (v1.3)

## Context

The app already had one deep module, `ChatConversation`, that talks to the
outside world only through `ReplySource`. We wanted to evaluate Google's ADK
for Kotlin as a real agent runtime: instructions, an event stream, a runner and
function tools. We did not want that experiment to leak into conversation
state, navigation or UI.

ADK Kotlin 1.4.0 is not a drop-in dependency for the previous build:

- its Android AARs declare `minCompileSdk=37`;
- it pulls Ktor 3.6.0, compiled with Kotlin 2.3.21 metadata, so the app cannot
  stay on Kotlin 2.0.21;
- AGP 9 embeds Kotlin 2.2.10, which still cannot read that metadata, so moving
  to AGP 9 would not solve the problem;
- the ADK and Google auth/transport jars add roughly 19 MB to the debug APK.

The alternative was either rebuilding the app around ADK or not evaluating it.
Both were worse than an adapter.

## Decision

Add ADK as a **second implementation of `ReplySource`**, never as a dependency
of `ChatConversation` or the UI.

1. Upgrade the toolchain to AGP 8.13.2, Gradle 8.14.6, Kotlin 2.3.21 and
   `compileSdk = 37`, keeping Java/Kotlin target 17 and `minSdk = 26`.
2. Add `AdkReplySource`, which builds one `LlmAgent` per turn with the shared
   `agentInstruction(agent)` prompt and a small `get_local_time` function tool.
3. Use ADK's OpenAI-compatible `ChatCompletions` model with OpenRouter's base
   URL, so both engines share one key, one model ID and one endpoint.
4. Create a fresh `InMemoryRunner` session each turn and replay the visible
   history as events. `ChatConversation` remains the single source of truth;
   clear, retry and stale-chunk isolation behave the same for both engines.
5. Stream ADK partial events as reply chunks. If an endpoint does not stream,
   emit ADK's final aggregate once.
6. Keep direct OpenRouter as the default engine. The settings dialog exposes
   `OPENROUTER | ADK` and persists the choice with the rest of the config.

## Consequences

- The domain and UI did not change. Removing ADK means deleting one adapter,
  two dependencies, the enum branch and the engine chip; the seam stays.
- The debug APK grows from ~19 MB to ~38 MB, and first build/compile time
  increases. Release builds should use R8 and a backend proxy before shipping.
- The project is pinned to a newer toolchain than most Compose samples. The
  `android.suppressUnsupportedCompileSdk=37` flag only silences AGP's warning;
  the trade-off is documented rather than hidden.
- `AdkReplySourceTest` covers the no-key / blank-history failure paths without
  network. `AdkLiveTest` is opt-in and verifies a real streamed reply through
  OpenRouter.
- Revisit this decision if ADK lowers its `minCompileSdk`, if it stops forcing
  a Kotlin 2.3 runtime, or if the app needs sessions that survive process
  death. Those would justify replacing the per-turn in-memory session with a
  persistent session service.
