package io.github.op7418.guizangtv;

/** Build-specific defaults; saved ordering always takes precedence. */
final class HomePreset {
    private HomePreset() { }

    static String[] ids(boolean legacy) {
        return legacy
                ? new String[]{"hdmi:1", "hdmi:2", "com.mb.android", "com.netease.filmlytv",
                    "com.xiaomi.mitv.smartshare", "com.mitv.tvhome"}
                : new String[]{"hdmi:2", "hdmi:1", "com.xiaodianshi.tv.yst",
                    "com.xiaomi.mitv.smartshare", "com.mitv.gallery", "com.mitv.tvhome"};
    }

    static String[] labels(boolean legacy) {
        return legacy
                ? new String[]{"Zidoo Z9X", "Nintendo Switch", "Emby", "网易 Filmly", "小米投屏", "小米原桌面"}
                : new String[]{"PlayStation 5", "Apple TV", "哔哩哔哩", "小米投屏", "相册", "小米原桌面"};
    }
}
