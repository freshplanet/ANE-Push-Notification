package com.freshplanet.nativeExtensions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * TODO this is all placeholder code. Don't use it
 */
public class SettingsLauncherActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(Extension.context != null) {
            //TODO see if there's any useful info I can pass here
            Extension.context.dispatchStatusEventAsync("COMPLETE", "");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
