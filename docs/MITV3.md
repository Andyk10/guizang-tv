# MiTV3 兼容构建与 Home 键

本仓库的本地适配分支。不是上游正式 Release，也不是所有老小米电视的通用版本。

## 设备与构建

- 型号：MiTV3；设备代号：inception。
- Android 5.1.1 / API 22；固件 7.6.9.1426；32 位 ARM。
- 桌面渲染为 1920 × 1080、density 320；不能据此推断物理面板规格。
- `-Pmitv3=true` 使用独立包名 `io.github.op7418.guizangtv.mitv3`，应用名“归藏电视 · MiTV3”。
- 不传参数时仍为 API 26 起的标准版，原包名与首页默认项不变。

工具版本仍为 JDK 17、Gradle 8.9、Android SDK Platform 34 / Build Tools 34.0.0：

```sh
gradle -Pmitv3=true :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :boot-helper:assembleDebug :boot-helper:lintDebug
```

输出仍在 `app/build/outputs/apk/debug/app-debug.apk`。标准版和 MiTV3 版共用构建输出目录，切换构建参数后请及时单独保存 APK，不要凭文件名判断版本；安装前核对包名、最低 API 和签名。

完整开机还需安装 `boot-helper/build/outputs/apk/debug/boot-helper-debug.apk`。这是同仓库的“归藏启动桥”，包名 `io.github.op7418.guizangtv.mitv3.boot`，没有系统权限、网络请求或后台服务，只转发给主应用的一次性启动入口。它只声明 INFO，不占用用户的普通桌面应用入口。主应用仍可正常从应用列表打开。

MiTV3 默认入口：Zidoo Z9X / HDMI 1、Nintendo Switch / HDMI 2、Emby、网易 Filmly、小米投屏、小米原桌面。已有首页顺序仍优先。两个 HDMI 图标为本分支绘制的通用媒体盒与手柄图形，不是品牌标志。

Java 集合默认方法与 `java.util.function` 使用 core library desugaring；系统权限检查兼容 API 22；不使用 API 23 的滚动监听接口。原矢量图中省略前导零的小数已经等值规范化，避免 Android 5.x 的路径解析问题。兼容版默认固定壁纸，可手动开启轮播；Codex 实验暂不开放，不迁移或修改配对。

## HDMI 与原生入口

从目标电视的系统 APK 静态检查到：

- 播放器 1.3.1 的 `ExternalSourceActivity` 接收 `EXTSRC_PLAY`，读取整数 `input`。
- 同一播放器代码中的信号源名称映射为 23 → HDMI、24 → HDMI2。
- 目标 Activity 有公开 intent-filter，未声明 Activity 或 application 级调用权限。
- 设置 APK 的 `SettingsBroadcastReceiver` 显式导出，包含 `INPUTSOURCE_POPUP`。

适配同时限制构建开关、型号、代号、固件和 API，不只是追加一个型号。调用时仍检查组件启用、导出和调用权限。HDMI 直达被拒绝时尝试原生信号源菜单；都不可用则提示用户使用原生菜单，不修改系统配置。

静态映射不等于已验证画面、声音或游戏延迟。普通应用启动测试也不等于 Zidoo / Switch 已唤醒；需要分别现场验证。

## Home 键：本机需要兼容返回

当前小米蓝牙遥控器使用 `Vendor_2717_Product_32b1.kl`，扫描码 102 映射到标准 HOME；红外接收器也使用 HOME。系统按键路径会发出 `CLOSE_SYSTEM_DIALOGS`，reason 为 `homekey`。

### 为什么默认桌面设置无法解决

“主屏幕”设置页中，上下聚焦整行并不能确认，需要点击左侧单选圆圈。经用户要求配置后，系统确实将归藏写入了首选记录；诊断 API `getHomeActivities()` 也返回归藏。但同一时刻 `resolveActivity(MAIN + HOME)` 和实际 Home 按键仍返回小米桌面。两个候选的 priority 均为 0，不能把问题简单归咎于可调的优先级。

因此此固件上不要反复尝试普通默认桌面选择，也不要将“记录已保存”当成 Home 已生效。

### 兼容开关

路径：**设置 → 电视与启动 → 遥控器 Home 键**。

- “开启 Home 兼容返回”：启动本机前台服务，注册系统 Home 通知。服务有常驻通知，不读屏、不读取应用内容，不申请无障碍、悬浮窗或使用情况访问权限。
- 只响应 `CLOSE_SYSTEM_DIALOGS / homekey`；不会因为打开小米桌面、进入 HDMI、返回键、音量或普通窗口变化而反复拉回归藏。注册时要求发送方持有系统 `INJECT_EVENTS` 权限；应用自身不申请此权限。
- 收到 Home 通知后延迟 400 ms，经显式 PendingIntent 返回主 Activity。实机发现直接 `startActivity()` 会进入旧系统的约五秒切换限制，故不采用直接启动路径。
- 返回后收起归藏自己的设置/对话层，聚焦上次首页入口；确认记录来自窗口获得焦点，不只是发出了启动请求。
- 关闭开关会注销接收器并停止前台服务；开启/关闭不改变首页顺序、壁纸或其他用户偏好。

这不是替换系统底层 Home 实现，某些情况下可能短暂经过小米桌面。首次安装默认关闭；只有已验证的 MiTV3 / Android 5.1.1 固件允许启用。标准版的服务组件禁用。安装更新后建议打开归藏一次，确认已保存的开关与兼容服务状态。

## 完整开机与恢复

同页的“开启开机进入归藏”独立保存。仅监听 `BOOT_COMPLETED` 的首轮测试没有达到目标：电视后来显示 Emby，归藏处于 stopped 状态，不能认为广播路径已通过。用户当时只是退出 Emby，并未关闭开机启动开关；本次没有修改 Emby 的设置或组件。

随后从本机已安装的小米桌面 3.49.1601 中核对到：BootActivity.finish 和主桌面启动路径优先读取 `Settings.System.start_3rd_app`，为空时回退到其私有缓存的自动启动应用；AppUtil.openAutoApp 使用 `getLaunchIntentForPackage()`。因此使用本机已存在的原生入口，而不是从新款小米照抄配置。

配置步骤（先记录旧值；替换实际设备地址）：

```sh
adb -s TV_IP:PORT install -r app/build/outputs/apk/debug/app-debug.apk
adb -s TV_IP:PORT install -r boot-helper/build/outputs/apk/debug/boot-helper-debug.apk
adb -s TV_IP:PORT shell settings get system start_3rd_app
adb -s TV_IP:PORT shell settings get system boot_auto_play
# 确认希望完整开机进入归藏后：
adb -s TV_IP:PORT shell settings put system start_3rd_app io.github.op7418.guizangtv.mitv3.boot
adb -s TV_IP:PORT shell settings put system boot_auto_play 0
```

然后在归藏设置中开启“开机进入归藏”和所需的 Home 兼容返回。启动桥调用主应用的 LegacyBootGateActivity；后者检查机型、用户开关、本次内核 boot_id 与开机时间。仅在完整开机十分钟内尝试一次，发送前持久化标记，重复或迟到的系统调用直接结束，不抢回正在使用的 HDMI。boot_id 只保存在应用私有偏好中，不输出到公共诊断。

已配置原生桥时，BOOT_COMPLETED 只负责恢复 Home 服务，不与原生入口争抢首次打开；未配置时保留受同一去重策略限制的广播备用路径。日常运行不依赖 ADB、电脑或网络服务。

原生桥方案经过一次完整重启验证：在未通过 ADB 手动启动归藏的情况下，应用于本次开机约 30.8 秒获得窗口焦点，记录来源为 native；约 32 秒的系统采样已显示归藏。之后 ADB 端口关闭，用户确认电视仍为归藏并重新开启 ADB；约 260 秒复核时仍为归藏主界面，Home 前台服务运行且用户偏好保留。此验证覆盖完整系统重启，不等同于断电再上电、待机唤醒或长期稳定性测试。

请关闭电视的“开机直接播放信号源”，避免 HDMI 自动播放抢占。应用不自行写系统设置；本次配置前记录为 `boot_auto_play=1`、`start_3rd_app` 未设置，用户授权目标行为后使用 `0` 和上述启动桥目标。

兼容版在 manifest 中禁用 BootGateActivity，确保按包名启动时不会先解析到新款小米的 INFO 入口然后静默结束；标准版仍保留该入口。

**待机唤醒不是完整开机。** 当前不监听 SCREEN_ON，不保证从 HDMI 待机唤醒也强制进入归藏，避免自动输入、CEC 和屏幕状态变化被误认为重启。冷启动、待机、CEC 的验证应分别记录。

恢复步骤：

1. 关闭“Home 兼容返回”和“开机进入归藏”；Home 服务立即停止。
2. 按原记录恢复 `start_3rd_app` 与开机信号源设置；原本不存在的键可删除，原本为空字符串则恢复空字符串，不把文本 null 写为包名。小米可能缓存启动桥目标，因此先关闭归藏的开机开关，并保留两个 APK 直到重启确认。若明确要恢复开机打开 Emby，可将原生目标设为 `com.mb.android`，无需清除小米桌面数据。
3. 如需清理首选记录，在原生“主屏幕”设置点小米桌面的左侧圆圈，或清除归藏的默认打开方式；不要清除任何应用数据。
4. 验证原有行为后，可卸载独立试用包。小米桌面和系统包一直保留，不需要批量 `pm enable`。

未请求 Root、刷机、修改键盘布局、停用广告/更新组件、开启 AI 或改动 CEC。调试结束后可关闭网络 ADB；Home 服务和开机进入均在电视本机运行。

## 验证记录

设备侧与构建测试的完成情况见 [本次变更记录](changes/2026-10-25-mitv3.md)。未完成实体遥控器、HDMI 画面和声音检查前，不将其标记为已适配全部使用路径。
