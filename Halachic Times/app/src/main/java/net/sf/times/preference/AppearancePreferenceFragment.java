package net.sf.times.preference;

import android.annotation.TargetApi;
import android.os.Build;

import net.sf.times.R;

/**
 * This fragment shows the preferences for the Appearance header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppearancePreferenceFragment extends DefaultPreferenceFragment {

    @Override
    protected int getPreferencesXml() {
        return R.xml.appearance_preferences;
    }
}
