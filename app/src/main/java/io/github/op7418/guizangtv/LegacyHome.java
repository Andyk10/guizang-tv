package io.github.op7418.guizangtv;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;
import java.io.BufferedReader;
import java.io.FileReader;

/** Explicitly enabled, firmware-scoped handoff. Does not edit system preferences. */
final class LegacyHome {
    private static final String EXTRA = "guizang.legacy-home-trigger";
    static final String BOOT_HELPER = "io.github.op7418.guizangtv.mitv3.boot";
    private LegacyHome() { }
    static boolean supported() { return BuildConfig.LEGACY_MITV3 && XiaomiControls.enabled(); }
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("legacy-home", 0); }
    static boolean home(Context c) { return supported() && prefs(c).getBoolean("home", false); }
    static boolean boot(Context c) { return supported() && prefs(c).getBoolean("boot", false); }
    static boolean nativeConfigured(Context c) {
        return BOOT_HELPER.equals(Settings.System.getString(c.getContentResolver(), "start_3rd_app"));
    }

    private static String bootIdentity() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/sys/kernel/random/boot_id"))) {
            String value = reader.readLine();
            return value != null && value.matches("[a-fA-F0-9-]{36}") ? value : null;
        } catch (java.io.IOException unavailable) { return null; }
    }

    static void requestBoot(Context c, String source) {
        if (!boot(c)) return;
        String identity = bootIdentity();
        if (identity == null) { status(c, "无法读取本次启动标识，已跳过自动进入"); return; }
        if (!LegacyHomePolicy.newBoot(true, identity,
                prefs(c).getString("boot_attempt", ""), SystemClock.elapsedRealtime())) return;
        // Persist before dispatch: repeated vendor calls must not steal an HDMI session.
        if (!prefs(c).edit().putString("boot_attempt", identity).putString("boot_source", source).commit()) return;
        launch(c, "boot");
    }

    static boolean set(Context c, String key, boolean value) {
        if (!supported() || (!"home".equals(key) && !"boot".equals(key))) return false;
        if (!prefs(c).edit().putBoolean(key, value).commit()) return false;
        sync(c);
        return true;
    }

    static void sync(Context c) {
        if (!supported()) return;
        Intent service = new Intent(c, LegacyHomeService.class);
        try {
            if (home(c)) c.startService(service); else c.stopService(service);
        } catch (RuntimeException failure) { status(c, "兼容服务启动失败：" + failure.getClass().getSimpleName()); }
    }

    static Intent intent(Context c) {
        return new Intent(c, FrameHomeActivity.class).setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
    }

    static void launch(Context c, String trigger) {
        if (!("home".equals(trigger) ? home(c) : "boot".equals(trigger) && boot(c))) return;
        status(c, ("home".equals(trigger) ? "Home 返回" : "开机进入") + "已请求，等待界面确认");
        try {
            // A system-dispatched PendingIntent avoids API 22's post-Home app-switch queue.
            PendingIntent.getActivity(c, "home".equals(trigger) ? 41 : 42,
                    intent(c).putExtra(EXTRA, trigger), PendingIntent.FLAG_CANCEL_CURRENT
                            | (android.os.Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)).send();
        } catch (PendingIntent.CanceledException | RuntimeException failure) {
            status(c, "打开归藏失败：" + failure.getClass().getSimpleName());
        }
    }

    static void confirm(Activity activity) {
        String trigger = activity.getIntent().getStringExtra(EXTRA);
        if (!supported() || (!"home".equals(trigger) && !"boot".equals(trigger))) return;
        activity.getIntent().removeExtra(EXTRA);
        status(activity, "home".equals(trigger) ? "Home 返回已显示" : "开机进入已显示");
        prefs(activity).edit().putLong("confirmed_at", SystemClock.elapsedRealtime()).apply();
    }

    static String status(Context c) { return prefs(c).getString("status", "尚未触发兼容返回"); }
    static void status(Context c, String text) {
        prefs(c).edit().putString("status", text).apply();
        android.util.Log.i("GuiZangLegacyHome", text);
    }
}
