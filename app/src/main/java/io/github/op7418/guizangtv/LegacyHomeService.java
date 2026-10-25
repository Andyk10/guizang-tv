package io.github.op7418.guizangtv;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

/** The old firmware sends Home notifications only to registered receivers. No screen access. */
public final class LegacyHomeService extends Service {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean registered;
    private final Runnable returnHome = () -> LegacyHome.launch(this, "home");
    private final BroadcastReceiver homeKey = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!LegacyHomePolicy.homeEvent(LegacyHome.supported(), LegacyHome.home(context),
                    intent.getAction(), intent.getStringExtra("reason"))) return;
            handler.removeCallbacks(returnHome);
            handler.postDelayed(returnHome, LegacyHomePolicy.HOME_DELAY_MS);
        }
    };

    // API 22 predates receiver export flags and foreground-service types/channels.
    @android.annotation.SuppressLint({"ForegroundServiceType", "UnspecifiedRegisterReceiverFlag"})
    @Override public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT != 22 || !LegacyHome.home(this)) { stopSelf(); return; }
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeKey, filter, "android.permission.INJECT_EVENTS", handler);
        registered = true;
        // This component is disabled outside the API 22 build and exact device profile.
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent open = PendingIntent.getActivity(this, 0, LegacyHome.intent(this), flags);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher).setContentTitle("归藏电视 · Home 兼容返回")
                .setContentText("仅接收 Home 系统通知，可在归藏设置中关闭")
                .setOngoing(true).setShowWhen(false).setContentIntent(open).build();
        startForeground(31, notification);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (!LegacyHome.home(this)) { stopSelf(); return START_NOT_STICKY; }
        return START_STICKY;
    }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (registered) unregisterReceiver(homeKey);
        stopForeground(true);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
