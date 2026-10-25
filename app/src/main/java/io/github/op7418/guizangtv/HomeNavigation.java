package io.github.op7418.guizangtv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

/** Observes the system's HOME resolver; never sets a default or intercepts keys. */
final class HomeNavigation {
    private HomeNavigation() { }

    static String status(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolved = pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolved == null || resolved.activityInfo == null) return "系统未返回桌面入口";
        String pkg = resolved.activityInfo.packageName;
        if (pkg.equals(context.getPackageName())) return "系统 HOME 解析到归藏电视；实体按键仍需验证";
        if ("android".equals(pkg)) return "系统桌面选择器；尚未确认默认桌面";
        return "系统 HOME 解析到：" + resolved.loadLabel(pm);
    }

    static Intent settingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
        ResolveInfo resolved = context.getPackageManager().resolveActivity(intent, 0);
        if (resolved == null || resolved.activityInfo == null || !resolved.activityInfo.exported
                || !resolved.activityInfo.enabled) return null;
        String permission = resolved.activityInfo.permission;
        return permission == null || context.checkCallingOrSelfPermission(permission)
                == PackageManager.PERMISSION_GRANTED ? intent : null;
    }
}
