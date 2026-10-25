package io.github.op7418.guizangtv;

import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.*;

public class HomePresetTest {
    @Test public void legacyPortsMatchTheActualCables() {
        String[] ids = HomePreset.ids(true), labels = HomePreset.labels(true);
        assertEquals("hdmi:1", ids[0]);
        assertEquals("Zidoo Z9X", labels[0]);
        assertEquals("hdmi:2", ids[1]);
        assertEquals("Nintendo Switch", labels[1]);
        assertEquals("com.mitv.tvhome", ids[ids.length - 1]);
    }
    @Test public void standardBuildKeepsItsExistingDefaults() {
        assertEquals("hdmi:2", HomePreset.ids(false)[0]);
        assertEquals("PlayStation 5", HomePreset.labels(false)[0]);
        assertEquals("Apple TV", HomePreset.labels(false)[1]);
    }
    @Test public void bothPresetsHaveDistinctLabelledEntries() {
        for (boolean legacy : new boolean[]{false, true}) {
            String[] ids = HomePreset.ids(legacy);
            assertEquals(ids.length, HomePreset.labels(legacy).length);
            assertEquals(ids.length, new HashSet<>(Arrays.asList(ids)).size());
            assertTrue(ids.length > 0 && ids.length <= 9);
        }
    }
    @Test public void callersCannotMutateFutureDefaults() {
        String[] ids = HomePreset.ids(true);
        ids[0] = "changed";
        assertEquals("hdmi:1", HomePreset.ids(true)[0]);
    }
}
