# AppInspector | 应用探测器

[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Author](https://img.shields.io/badge/Author-EraserCN-orange.svg)](https://github.com/EraserCN)

[English](#english) | [简体中文](#chinese)

---

<a name="english"></a>
## English

**AppInspector** is a powerful Android diagnostic and forensic tool designed to explore and verify all possible methods of discovering installed applications.

In modern Android versions, "App Visibility" is increasingly restricted by the system. This project demonstrates how different API surfaces, Intent filters, and Root-level access "see" the system's application landscape, helping developers and security researchers understand the boundaries of app enumeration.

### 🚀 Key Features

The app categorizes its inspection methods into several dimensions based on `QueryMethod.kt`:

*   **Standard PackageManager**: 
    *   Direct calls like `getInstalledPackages` and `getInstalledApplications`.
    *   Flag-based discovery (e.g., `MATCH_UNINSTALLED_PACKAGES`).
    *   Individual package info polling for known lists.
*   **Intent-Based Discovery (Launcher & Apps)**:
    *   Categorized discovery: Launcher, Home, Leanback (TV), Car/Desk docks.
    *   Common app categories: Calculator, Calendar, Maps, Settings, etc.
*   **Media & Content Handlers**:
    *   Detecting apps that handle `ACTION_VIEW` for images, videos, audio, and PDFs.
    *   Capture intents (`IMAGE_CAPTURE`, `VIDEO_CAPTURE`).
*   **Sharing & Communication**:
    *   Apps responding to `ACTION_SEND` (Sharing text/images).
    *   Communication intents (Dial, Call, SMS, Contacts).
*   **System Services & Integration**:
    *   Identification via Accessibility Services, Input Methods (IME), and Sync Adapters.
    *   Device Admins, Wallpapers, and Print Services.
*   **Root-Level Inspection** (Requires Su):
    *   Directly listing directories: `/data/app`, `/system/app`, `/system/priv-app`, `/product/app`.
    *   Parsing the system's internal registry: `/data/system/packages.xml`.
    *   Executing low-level shell commands: `pm list packages -a`.

### 🛠 Tech Stack
- **Language**: Kotlin
- **UI**: Material Design 3, ViewBinding, RecyclerView
- **Architecture**: Jetpack (ViewModel, Lifecycle)
- **Root Support**: [libsu](https://github.com/topjohnwu/libsu)
- **Concurrency**: Kotlin Coroutines

---

<a name="chinese"></a>
## 简体中文

**AppInspector (应用探测器)** 是一款强大的 Android 诊断与分析工具，旨在探索和验证在 Android 设备上发现已安装应用的所有可能方法。

在现代 Android 系统中，“应用可见性”受到系统层面的严格限制。本项目旨在展示不同的 API 层面、Intent 过滤器以及 Root 级别权限是如何“看待”系统中的应用分布的，帮助开发者和安全研究人员了解应用枚举的边界。

### 🚀 核心功能

应用根据 `QueryMethod.kt` 将探测方法分为以下几个维度：

*   **标准 PackageManager**: 
    *   直接调用 `getInstalledPackages` 和 `getInstalledApplications`。
    *   基于 Flag 的探测（例如 `MATCH_UNINSTALLED_PACKAGES` 查找卸载残留）。
    *   对已知列表进行逐包查询 (`getPackageInfo`)。
*   **基于 Intent 的发现 (启动器与应用)**:
    *   分类探测：常规启动器、桌面、电视 (Leanback)、车载/桌面底座模式。
    *   常用应用类别：计算器、日历、地图、设置等。
*   **媒体与内容处理**:
    *   检测处理图片、视频、音频、PDF 的 `ACTION_VIEW` 应用。
    *   捕获意图（拍照、录像）。
*   **分享与通信**:
    *   响应 `ACTION_SEND`（分享文字/图片）的应用。
    *   通信意图（拨号、通话、短信、联系人）。
*   **系统服务与集成**:
    *   通过无障碍服务 (Accessibility)、输入法 (IME) 和同步适配器 (Sync Adapters) 进行识别。
    *   设备管理器、壁纸服务、打印服务等。
*   **Root 深度探测** (需要 Root 权限):
    *   直接枚举目录: `/data/app`, `/system/app`, `/system/priv-app`, `/product/app`。
    *   解析系统内部注册表: `/data/system/packages.xml`。
    *   执行底层 Shell 命令: `pm list packages -a`。

### 🛠 技术栈
- **编程语言**: Kotlin
- **UI 框架**: Material Design 3, ViewBinding, RecyclerView
- **架构组件**: Jetpack (ViewModel, Lifecycle)
- **Root 支持**: [libsu](https://github.com/topjohnwu/libsu)
- **异步处理**: Kotlin Coroutines

---

## Author

**EraserCN**
- GitHub: [@EraserCN](https://github.com/EraserCN)

## License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](LICENSE) file for details.
