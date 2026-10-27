# 壁纸张数上限提升到 30 并打包个人壁纸

## 范围与安全边界

- 只改壁纸张数上限与本地壁纸打包，不动播放、Home、HDMI、开机、天气、外观的既有逻辑。
- 目标实机仍是 MiTV3 / inception / Android 5.1.1（API 22）/ 固件 7.6.9.1426，兼容版 versionCode 10 不变。
- 不新增电视端导入入口，不引入网络壁纸，不上传任何图片。
- 图片只放进被 Git 忽略的 `app/src/main/assets/wallpapers/local/`，不进入公开源码。

## 为什么改上限

原有 `1–15` 两处硬编码（`scripts/prepare_wallpapers.py` 的参数校验、`FrameWallpapers.java` 读取 catalog 时的长度校验）互相独立。只改脚本会在电视端静默失败：`FrameWallpapers` 构造时抛 `IllegalArgumentException`，被 `catch` 吞掉后 `entries` 为空，随后回退到五张内置渐变色块，用户看到的是“壁纸没换”而不是报错。

因此本次同时放宽两处，并把数值提成具名常量，附上互相引用的注释，避免以后再漏改一处。

## 为什么不改成 3840

此前实机检查中，`wm size` 与 `dumpsys display` 都报告唯一显示模式为 1920×1080，无 4K 模式可选。`FrameWallpapers.decode()` 按 `DisplayMetrics` 传入的宽高做中心裁剪与 `inSampleSize` 降采样，并用 `BitmapRegionDecoder` 只解可见区域。因此本次统一为 16:9 的图片保留 1920×1080 输出即可匹配渲染目标；更大的源图通常只增加包体积，不能据此保证完全没有重采样差异或断言体积固定翻倍。保持脚本现有的 1920 上限。

## 实现

1. `scripts/prepare_wallpapers.py`：新增 `MAX_WALLPAPERS = 30`，参数校验与帮助文本都引用该常量。
2. `FrameWallpapers.java`：新增 `static final int MAX_WALLPAPERS = 30`，读取 catalog 时用它校验；注释标明需与脚本保持一致。
3. `README.md`、`docs/PROJECT-GUIDE.zh-CN.md`：文档里的 `1–15` 同步改为 `1–30`。

## 打包内容

20 张，全部统一为 16:9 后由脚本压到最长边 1920、JPEG quality 88，缩略图 320×180：

- macOS 官方壁纸 4 张：卡特琳娜岛、莫哈韦沙丘、红杉林晨光、太浩湖暮色。其中卡特琳娜岛原图为 1:1，打包前居中裁成 16:9。
- Unsplash 照片 16 张：林间木栈道、晨光森林、金色麦田日落、草原落日、孤松、雪原疏树、山谷湖光、翡翠湖、雾涌林海、雪山荒原、层峦叠嶂、秋林晨雾、沙丘之脊、冰河湖、暗色褶皱、蓝玫瑰。

原始素材尺寸据打包记录为 3840 宽以上（一张 6000×4000、三张 6016 宽），本次审查未重新核验原始文件。审查开始时，40 张生成图片合计 9,445,171 字节（约 9.01 MiB），本地 APK 为 12,661,053 字节（约 12.07 MiB）；不把这部分存储占用等同于运行内存占用。

macOS 壁纸版权归 Apple Inc.，仅作本机个人使用，未随源码提交。

## 原实施者的构建与实机记录

以下安装、日志和截图结论沿用原实施者 ZCode 的记录；Codex 审查不代表重新安装或逐项重做实机验收。

- `gradle -Pmitv3=true assembleDebug` 成功；产物包名 `io.github.op7418.guizangtv.mitv3`、minSdk 22、versionName `0.4.0-dev-mitv3.4`。注意必须写 `-Pmitv3=true`：漏掉时会静默回退到标准版（minSdk 26），不报错。
- APK 内 `assets/wallpapers/local/` 含 41 个文件（20 主图 + 20 缩略图 + catalog.json），从 APK 中抽出的 `catalog.json` 确认为 20 条且名称已为中文。
- 覆盖安装成功，未卸载、未清数据。重启归藏后日志 `I/TvHome.Wallpaper: Applied macos_catalina rotating=true minutes=5`，截图确认壁纸铺满全屏、左侧应用列表毛玻璃正常。
- 设置页截图确认「壁纸」项显示 `01_卡特琳娜岛`，「轮播」显示「每 5 分钟」。
- 壁纸选择列表截图确认中文名正常渲染、右侧实时预览可用。

## 2026-10-27 审查补充

- 上限两端同步为 30 的方向正确，Home、HDMI、开机、天气和内存缓存逻辑未改变。
- 复现旧测试仍把 16 张当作超限导致的失败；补上 16/20/30 张成功、31 张拒绝、非法数量不改变现有图片包以及两端常量一致性检查。
- 复现 Windows 默认 GBK 编码下，含非 BMP 字符的文件名触发 `UnicodeEncodeError`；catalog 改为明确使用 UTF-8 写入，避免中文乱码或写入失败。
- `python -m unittest scripts.test_prepare_wallpapers -v`：7 项测试全部通过，包含中文与非 BMP 字符文件名、原图不变、尺寸上限、旧包备份与失败保护。测试只生成临时素材，不重新处理个人壁纸。
- 只读检查确认 20 个唯一 ID、20 张 1920×1080 主图、20 张 320×180 缩略图均可解码；审查开始时的本地 APK 与全部 41 个本地素材文件逐字节匹配。
- 检查环境为 Windows 10 build 19045、Python 3.14.4、Pillow 12.2.0、Temurin JDK 17、Gradle 8.9、Android SDK 34 / Build Tools 34.0.0。原本地 APK 为 MiTV3 独立包、minSdk 22、versionCode 10，使用 Android Debug 证书；本轮不连接电视核对已安装包签名，不执行覆盖安装。
- 分别使用 `-Pmitv3=false` 和 `-Pmitv3=true`，执行 `gradle --offline --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain`，两个版本均构建成功，各 55 项单元测试通过，无失败或跳过。静态检查均为 0 错误，标准版 29 条警告、MiTV3 版 25 条警告；MiTV3 警告的类型、文本与数量和之前的审查报告一致。
- 构建后核对，全部 41 个本地素材文件的 SHA-256 与构建前一致，并与新 APK 中的文件一致。最终本地 APK 仍为 MiTV3 包名、minSdk 22、versionCode 10，签名证书与构建前一致；保留了构建前 APK 的本地备份。
- 个人素材及历史素材包仍被 Git 忽略；提交不包含图片、APK、构建缓存或本地检查报告。`git diff --check` 通过。

## 未覆盖与回退

- 本次未操作电视。原实施记录仅覆盖默认加载、列表渲染与预览，未逐张切换全部 20 张；轮播 5 分钟间隔未做长时间观察。
- 未验证遥控器在壁纸列表中的完整上下滚动边界。
- 回退：先将 `app/src/main/assets/wallpapers/local/` 完整备份到 assets 目录之外，再从构建输入中移出该目录并重新构建，即回到五张内置照片；或恢复上一份生成结果。保留个人图片，不直接删除；历史包保存在本地 `.wallpaper-backups/previous-*`（不入 Git）。

原实现：ZCode。审查、补测与提交：Codex。Agent：`codex`。Change-Id：`guizang-tv-20261026-wallpaper-pack`。Agent-Session：`codex:01a17c98-31d9-7c93-a951-a1eabc84f3c0`。承接对话 `01a1719b-8e7a-7df0-9f37-6182847639d2` 中的中文提交请求；不推送远程，不发布 Release。
