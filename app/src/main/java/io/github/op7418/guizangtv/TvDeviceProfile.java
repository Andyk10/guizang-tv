package io.github.op7418.guizangtv;

/** Keep legacy source adaptation separate from the newer TV's startup hook. */
final class TvDeviceProfile {
    private TvDeviceProfile() { }

    static boolean sources(boolean legacy, String model, String device, String firmware, int sdk) {
        if (!legacy) return "MiTV-MTSU0".equals(model);
        return "MiTV3".equals(model) && "inception".equals(device)
                && "7.6.9.1426".equals(firmware) && sdk == 22;
    }

    static int hdmiInput(int port) {
        // The inspected MiTV3 player maps 23/24 to HDMI/HDMI2 and reads an integer extra.
        return port == 1 ? 23 : port == 2 ? 24 : -1;
    }
}
