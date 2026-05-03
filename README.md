# VoicePrompter Android App

智能语音提词器 APK 源码。

## 一、快速获取 APK（推荐）

### 方案 A：GitHub Actions 自动构建

1. 在 GitHub 创建新仓库
2. 将 `android/` 文件夹推送到仓库
3. 进入 Actions 标签页，手动运行 "Build VoicePrompter APK"
4. 构建完成后，在 Actions 运行结果中下载 `VoicePrompter-APK.zip`
5. 解压得到 `app-debug.apk`，安装到手机即可

### 方案 B：用 Android Studio 构建

1. 安装 [Android Studio](https://developer.android.com/studio)
2. 打开项目：File → Open → 选择 `android/` 文件夹
3. 等待 Gradle 同步完成
4. 点击 Run ▶ 按钮（或 Build → Build Bundle(s) / APK(s) → Build APK）
5. APK 生成在 `app/build/outputs/apk/debug/app-debug.apk`

## 二、安装到手机

1. 将 `app-debug.apk` 传到手机（微信/QQ/数据线）
2. 手机设置中开启"允许安装未知来源应用"
3. 点击 APK 文件安装
4. 打开 VoicePrompter
5. 首次运行会弹出"麦克风权限"对话框 → 点击"允许"
6. 粘贴讲稿 → 开始朗读

## 三、技术说明

- 使用 Android 系统 SpeechRecognizer（Google 语音服务），比浏览器稳定得多
- 通过 WebView 加载本地 HTML，无需网络
- 语音识别结果通过 JavaScript Bridge 实时回调
- 支持中英文语音识别
