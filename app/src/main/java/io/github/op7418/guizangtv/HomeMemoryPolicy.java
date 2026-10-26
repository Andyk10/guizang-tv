package io.github.op7418.guizangtv;

final class HomeMemoryPolicy {
    static final int MATERIAL_BYTES = 2 * 1024 * 1024;
    private HomeMemoryPolicy() { }
    static boolean release(int level, boolean visible) {
        // Android ComponentCallbacks2: RUNNING_LOW=10, RUNNING_CRITICAL=15, BACKGROUND=40.
        return !visible && (level == 10 || level == 15 || level >= 40);
    }
}
