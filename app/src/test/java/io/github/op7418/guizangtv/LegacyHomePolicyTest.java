package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyHomePolicyTest {
    private static final String CLOSE = "android.intent.action.CLOSE_SYSTEM_DIALOGS";
    @Test public void onlyAnEnabledHomeKeyNotificationReturnsHome() {
        assertTrue(LegacyHomePolicy.homeEvent(true, true, CLOSE, "homekey"));
        assertFalse(LegacyHomePolicy.homeEvent(false, true, CLOSE, "homekey"));
        assertFalse(LegacyHomePolicy.homeEvent(true, false, CLOSE, "homekey"));
        assertFalse(LegacyHomePolicy.homeEvent(true, true, CLOSE, "recentapps"));
        assertFalse(LegacyHomePolicy.homeEvent(true, true, CLOSE, "globalactions"));
        assertFalse(LegacyHomePolicy.homeEvent(true, true, CLOSE, null));
        assertFalse(LegacyHomePolicy.homeEvent(true, true, "other", "homekey"));
    }
    @Test public void bootAndStandbyAreDifferentEvents() {
        assertTrue(LegacyHomePolicy.bootEvent(true, true, "android.intent.action.BOOT_COMPLETED"));
        assertFalse(LegacyHomePolicy.bootEvent(true, false, "android.intent.action.BOOT_COMPLETED"));
        assertFalse(LegacyHomePolicy.bootEvent(false, true, "android.intent.action.BOOT_COMPLETED"));
        assertFalse(LegacyHomePolicy.bootEvent(true, true, "android.intent.action.SCREEN_ON"));
        assertFalse(LegacyHomePolicy.bootEvent(true, true, "android.intent.action.USER_PRESENT"));
        assertFalse(LegacyHomePolicy.bootEvent(true, true, null));
    }
    @Test public void nativeBootIsBoundedAndCannotRepeat() {
        assertTrue(LegacyHomePolicy.newBoot(true, "new", "old", 45000));
        assertTrue(LegacyHomePolicy.newBoot(true, "new", "old", 600000));
        assertFalse(LegacyHomePolicy.newBoot(true, "new", "new", 45000));
        assertFalse(LegacyHomePolicy.newBoot(false, "new", "old", 45000));
        assertFalse(LegacyHomePolicy.newBoot(true, null, "old", 45000));
        assertFalse(LegacyHomePolicy.newBoot(true, "", "old", 45000));
        assertFalse(LegacyHomePolicy.newBoot(true, "new", "old", 600001));
        assertFalse(LegacyHomePolicy.newBoot(true, "new", "old", -1));
    }
}
