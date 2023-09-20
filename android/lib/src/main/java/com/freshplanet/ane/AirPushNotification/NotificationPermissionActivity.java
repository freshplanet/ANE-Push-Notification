/**
 * Copyright 2017 FreshPlanet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freshplanet.ane.AirPushNotification;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationPermissionActivity extends Activity {
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
            else {
                Extension.context.dispatchStatusEventAsync("NOTIFICATION_SETTINGS_ENABLED", "");
                finish();
            }
        }
        else {
            finish();
        }
	}
    
    @Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		int permissionCheck = PackageManager.PERMISSION_GRANTED;
		for (int permission : grantResults) {
			permissionCheck = permissionCheck + permission;
		}
		if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
			// granted
			Extension.context.dispatchStatusEventAsync("NOTIFICATION_SETTINGS_ENABLED", "");
		} else {
			// denied - do nothing
			Extension.context.dispatchStatusEventAsync("NOTIFICATION_SETTINGS_DISABLED", "");
		}
        finish();
	}
}
