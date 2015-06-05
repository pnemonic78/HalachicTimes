package net.sf.times.preference;

import android.annotation.TargetApi;
import android.os.Build;

import net.sf.times.R;

/**
 * This fragment shows the preferences for the Privacy and Security header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PrivacyPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected int getPreferencesXml() {
        return R.xml.privacy_preferences;
    }
}
