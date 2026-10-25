package io.github.op7418.guizangtv;

import android.app.Activity;
import android.os.Bundle;

/** Separate from LAUNCHER/INFO so skipping a cached system hook never breaks manual launch. */
public final class LegacyBootGateActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            if (LegacyHome.supported()) {
                LegacyHome.sync(this);
                LegacyHome.requestBoot(this, "native");
            }
        } finally { finish(); }
    }
}
