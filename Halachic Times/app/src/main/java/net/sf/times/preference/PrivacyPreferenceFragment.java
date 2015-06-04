package net.sf.times.preference;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;

import net.sf.times.R;

/**
 * This fragment shows the preferences for the Privacy and Security header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PrivacyPreferenceFragment extends DefaultPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.privacy_preferences);
    }
}
