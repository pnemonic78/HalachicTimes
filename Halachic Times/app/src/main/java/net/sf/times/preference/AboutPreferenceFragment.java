package net.sf.times.preference;

import android.annotation.TargetApi;
import android.os.Build;

import net.sf.times.R;

/**
 * This fragment shows the preferences for the About header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AboutPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected int getPreferencesXml() {
        return R.xml.about_preferences;
    }
}
