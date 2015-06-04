package net.sf.times.preference;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * This fragment shows the preferences for a header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class DefaultPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int xmlId = getPreferencesXml();
        PreferenceManager.setDefaultValues(getActivity(), xmlId, false);
        addPreferencesFromResource(xmlId);
    }

    protected abstract int getPreferencesXml();
}
