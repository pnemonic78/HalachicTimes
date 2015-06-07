package net.sf.times.preference;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

import net.sf.preference.SeekBarDialogPreference;
import net.sf.times.R;

/**
 * This fragment shows the preferences for the Candles zman screen.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanCandlesPreferenceFragment extends ZmanPreferenceFragment {

    private SeekBarDialogPreference candles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        candles = (SeekBarDialogPreference) findPreference(ZmanimSettings.KEY_OPINION_CANDLES);
        candles.setSummary(R.plurals.candles_summary);
        candles.setOnPreferenceChangeListener(this);
        onCandlesPreferenceChange(candles, null);
    }


    private void onCandlesPreferenceChange(SeekBarDialogPreference preference, Object newValue) {
        int minutes = preference.getProgress();
        Resources res = getResources();
        CharSequence summary = res.getQuantityString(R.plurals.candles_summary, minutes, minutes);
        preference.setSummary(summary);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == candles) {
            onCandlesPreferenceChange(candles, newValue);
            return true;
        }
        return super.onPreferenceChange(preference, newValue);
    }
}
