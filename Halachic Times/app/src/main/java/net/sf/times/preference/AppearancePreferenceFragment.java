package net.sf.times.preference;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;

import net.sf.times.R;

/**
 * This fragment shows the preferences for the Appearance header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppearancePreferenceFragment extends DefaultPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_preferences);
    }
}
