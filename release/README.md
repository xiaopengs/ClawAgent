# 下载与安装

## v1.4 debug APK

- 文件：[`ClawAgent-v1.4-debug.apk`](ClawAgent-v1.4-debug.apk)
- 版本：v1.4（versionCode 5）
- 最低系统：Android 8.0（API 26）
- 签名：Android debug keystore
- SHA-256：`db125520515e82b0300d05c2e60bc52349922175375e0ef8f38cf68d461e5db1`

该包使用空 OpenRouter Key 构建，安装后可在 App 的「模型设置」中填入自己的 Key；也可以接入 Ollama / LM Studio / vLLM 等本地 OpenAI 兼容端点。

```bash
adb install -r ClawAgent-v1.4-debug.apk
```

> 这是本地试验用的 debug 包，不适合直接上架或对外分发。
