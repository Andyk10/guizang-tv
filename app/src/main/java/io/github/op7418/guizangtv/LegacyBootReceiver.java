package io.github.op7418.guizangtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/** A single handoff after a real boot; never reacts to screen-on or standby wakeups. */
public final class LegacyBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || !LegacyHome.supported()) return;
        LegacyHome.sync(context);
        // Prefer the vendor's post-startup entry over an early broadcast that Emby/stock can cover.
        if (LegacyHome.nativeConfigured(context)) return;
        if (!LegacyHomePolicy.bootEvent(true, LegacyHome.boot(context), intent.getAction())) return;
        PendingResult pending = goAsync();
        Context app = context.getApplicationContext();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try { LegacyHome.requestBoot(app, "broadcast"); } finally { pending.finish(); }
        }, LegacyHomePolicy.BOOT_DELAY_MS);
    }
}
