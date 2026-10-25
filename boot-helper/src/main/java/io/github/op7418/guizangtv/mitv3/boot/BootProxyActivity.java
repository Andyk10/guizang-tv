package io.github.op7418.guizangtv.mitv3.boot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/** No permissions or preferences: the main app owns all opt-in and once-per-boot checks. */
public final class BootProxyActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            if (Build.VERSION.SDK_INT == 22 && "MiTV3".equals(Build.MODEL)
                    && "inception".equals(Build.DEVICE) && "7.6.9.1426".equals(Build.DISPLAY)) {
                startActivity(new Intent().setComponent(new ComponentName(
                        "io.github.op7418.guizangtv.mitv3", "io.github.op7418.guizangtv.LegacyBootGateActivity"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
            }
        } catch (RuntimeException unavailable) {
            android.util.Log.w("GuiZangBootProxy", "Main app unavailable; leaving stock home unchanged", unavailable);
        } finally { finish(); }
    }
}
