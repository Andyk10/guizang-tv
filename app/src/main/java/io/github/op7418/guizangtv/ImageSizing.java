package io.github.op7418.guizangtv;

/** Decode near the requested pixel budget, then fit without upscaling. */
final class ImageSizing {
    static final int PREVIEW_WIDTH = 960, PREVIEW_HEIGHT = 540;
    private ImageSizing() { }
    static int[] fit(int width, int height, int maxWidth, int maxHeight) {
        validate(width, height, maxWidth, maxHeight);
        double scale = Math.min(1d, Math.min(maxWidth / (double) width, maxHeight / (double) height));
        return new int[]{Math.max(1, (int) Math.floor(width * scale)), Math.max(1, (int) Math.floor(height * scale))};
    }
    static int[] crop(int width, int height, int maxWidth, int maxHeight) {
        validate(width, height, maxWidth, maxHeight);
        double scale = Math.min(1d, Math.min(width / (double) maxWidth, height / (double) maxHeight));
        return new int[]{Math.max(1, (int) Math.floor(maxWidth * scale)), Math.max(1, (int) Math.floor(maxHeight * scale))};
    }
    static int cropSample(int width, int height, int targetWidth, int targetHeight) {
        validate(width, height, targetWidth, targetHeight);
        int sample = 1;
        while (width / (sample * 2L) >= targetWidth && height / (sample * 2L) >= targetHeight) sample *= 2;
        return sample;
    }
    static int[] cropRegion(int width, int height, int targetWidth, int targetHeight) {
        validate(width, height, targetWidth, targetHeight);
        int w = (int) Math.min(width, Math.ceil(height * (targetWidth / (double) targetHeight)));
        int h = (int) Math.min(height, Math.ceil(width * (targetHeight / (double) targetWidth)));
        int left = (width - w) / 2, top = (height - h) / 2;
        return new int[]{left, top, left + w, top + h};
    }
    private static void validate(int w, int h, int mw, int mh) {
        if (w <= 0 || h <= 0 || mw <= 0 || mh <= 0) throw new IllegalArgumentException("Invalid image bounds");
    }
}
