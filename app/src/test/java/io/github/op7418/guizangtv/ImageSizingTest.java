package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class ImageSizingTest {
    @Test public void centerCropRetainsMainRenderingResolution() {
        assertArrayEquals(new int[]{0,100,1920,1180},ImageSizing.cropRegion(1920,1280,1920,1080));
        assertArrayEquals(new int[]{0,100,1920,1180},ImageSizing.cropRegion(1920,1280,960,540));
        assertArrayEquals(new int[]{1920,1080},ImageSizing.crop(1920,1280,1920,1080));
        assertEquals(1,ImageSizing.cropSample(1920,1280,1920,1080));
        assertArrayEquals(new int[]{960,540},ImageSizing.crop(1920,1280,960,540));
        assertEquals(2,ImageSizing.cropSample(1920,1280,960,540));
    }
    @Test public void cropHandlesPortraitWithoutUpscaling() {
        assertArrayEquals(new int[]{0,1331,600,1669},ImageSizing.cropRegion(600,3000,960,540));
        assertArrayEquals(new int[]{600,337},ImageSizing.crop(600,3000,960,540));
        assertEquals(1,ImageSizing.cropSample(600,3000,600,337));
        assertArrayEquals(new int[]{320,180},ImageSizing.crop(320,180,960,540));
    }
    @Test public void previewDecodesQuarterPixelsInsteadOfFullHd() {
        assertEquals(2, ImageSizing.cropSample(1920,1080,960,540));
        assertArrayEquals(new int[]{960,540}, ImageSizing.fit(1920,1080,960,540));
    }
    @Test public void ultraHdMainMatchesFullHdRendering() {
        assertEquals(2, ImageSizing.cropSample(3840,2160,1920,1080));
        assertArrayEquals(new int[]{1920,1080}, ImageSizing.fit(3840,2160,1920,1080));
    }
    @Test public void neverUpscalesSmallImages() {
        assertEquals(1, ImageSizing.cropSample(320,180,960,540));
        assertArrayEquals(new int[]{320,180}, ImageSizing.fit(320,180,960,540));
    }
    @Test public void boundsPortraitAndPanoramicImages() {
        assertArrayEquals(new int[]{108,540}, ImageSizing.fit(600,3000,960,540));
        assertArrayEquals(new int[]{960,60}, ImageSizing.fit(8000,500,960,540));
    }
    @Test public void extremeRatiosKeepAtLeastOnePixel() {
        assertArrayEquals(new int[]{960,1}, ImageSizing.fit(Integer.MAX_VALUE,1,960,540));
        assertEquals(1073741824, ImageSizing.cropSample(Integer.MAX_VALUE,Integer.MAX_VALUE,1,1));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidDecodedBounds() {
        ImageSizing.cropSample(-1,0,960,540);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsInvalidTargetBounds() {
        ImageSizing.fit(1920,1080,0,540);
    }
}
