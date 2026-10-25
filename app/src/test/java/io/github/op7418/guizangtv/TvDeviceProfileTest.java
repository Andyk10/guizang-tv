package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class TvDeviceProfileTest {
    @Test public void inspectedLegacyFirmwareIsRecognized() {
        assertTrue(TvDeviceProfile.sources(true, "MiTV3", "inception", "7.6.9.1426", 22));
    }
    @Test public void unknownLegacyFirmwareAndLookalikesStayBlocked() {
        assertFalse(TvDeviceProfile.sources(true, "MiTV3", "inception", "other", 22));
        assertFalse(TvDeviceProfile.sources(true, "MiTV3", "other", "7.6.9.1426", 22));
        assertFalse(TvDeviceProfile.sources(true, "MiTV3S", "inception", "7.6.9.1426", 22));
        assertFalse(TvDeviceProfile.sources(true, "MiTV3", "inception", "7.6.9.1426", 23));
    }
    @Test public void buildsDoNotAccidentallyExpandEachOthersProfiles() {
        assertFalse(TvDeviceProfile.sources(false, "MiTV3", "inception", "7.6.9.1426", 22));
        assertFalse(TvDeviceProfile.sources(true, "MiTV-MTSU0", "", "", 34));
        assertTrue(TvDeviceProfile.sources(false, "MiTV-MTSU0", "", "", 34));
    }
    @Test public void onlyConfiguredPhysicalPortsHaveSourceIds() {
        assertEquals(23, TvDeviceProfile.hdmiInput(1));
        assertEquals(24, TvDeviceProfile.hdmiInput(2));
        assertEquals(-1, TvDeviceProfile.hdmiInput(0));
        assertEquals(-1, TvDeviceProfile.hdmiInput(3));
    }
}
