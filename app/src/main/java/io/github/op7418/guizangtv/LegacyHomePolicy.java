package io.github.op7418.guizangtv;

final class LegacyHomePolicy {
    static final long HOME_DELAY_MS = 400;
    static final long BOOT_DELAY_MS = 1500;
    private LegacyHomePolicy() { }

    static boolean homeEvent(boolean supported, boolean enabled, String action, String reason) {
        return supported && enabled && "android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)
                && "homekey".equals(reason);
    }

    static boolean bootEvent(boolean supported, boolean enabled, String action) {
        return supported && enabled && "android.intent.action.BOOT_COMPLETED".equals(action);
    }

    static boolean newBoot(boolean enabled, String identity, String attempted, long uptime) {
        return enabled && identity != null && !identity.isEmpty() && !identity.equals(attempted)
                && uptime >= 0 && uptime <= 10 * 60_000L;
    }
}
