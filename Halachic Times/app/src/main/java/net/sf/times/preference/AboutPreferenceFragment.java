package net.sf.times.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity();
        Preference version = findPreference("about.version");
        try {
            version.setSummary(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // Never should happen with our own package!
        }
    }
}
