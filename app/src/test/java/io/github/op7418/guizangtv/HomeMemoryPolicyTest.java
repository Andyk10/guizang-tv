package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class HomeMemoryPolicyTest {
    @Test public void neverDropsVisibleMaterials() {
        for(int level:new int[]{5,10,15,20,40,60,80}) assertFalse(HomeMemoryPolicy.release(level,true));
    }
    @Test public void ordinaryBackgroundTransitionKeepsFastReturn() {
        for(int level:new int[]{0,5,20}) assertFalse(HomeMemoryPolicy.release(level,false));
    }
    @Test public void pressureReleasesOnlyWhenNotVisible() {
        for(int level:new int[]{10,15,40,60,80}) assertTrue(HomeMemoryPolicy.release(level,false));
    }
    @Test public void cacheBudgetIsTwoMebibytes() {
        assertEquals(2097152,HomeMemoryPolicy.MATERIAL_BYTES);
    }
}
