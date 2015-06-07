package net.sf.times.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;

import net.sf.times.ZmanimReminder;

/**
 * This fragment shows the preferences for a zman screen.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanPreferenceFragment extends AbstractPreferenceFragment {

    public static final String EXTRA_XML = "xml";
    public static final String EXTRA_OPINION = "opinion";
    public static final String EXTRA_REMINDER = "reminder";

    private int xmlId;
    private String reminderKey;
    private ZmanimSettings settings;
    private ZmanimReminder reminder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String xmlName = args.getString(EXTRA_XML);
        Resources res = getResources();
        this.xmlId = res.getIdentifier(xmlName, "xml", getActivity().getPackageName());
        String opinionKey = args.getString(EXTRA_OPINION);
        this.reminderKey = args.getString(EXTRA_REMINDER);

        super.onCreate(savedInstanceState);

        initList(opinionKey);
        initList(reminderKey);
    }

    @Override
    protected int getPreferencesXml() {
        return xmlId;
    }

    @Override
    protected void onListPreferenceChange(ListPreference preference, Object newValue) {
        String oldValue = preference.getValue();

        super.onListPreferenceChange(preference, newValue);

        if (!oldValue.equals(newValue)) {
            if (preference.getKey().equals(reminderKey)) {
                Context context = getActivity();
                if (settings == null)
                    settings = new ZmanimSettings(context);
                if (reminder == null)
                    reminder = new ZmanimReminder(context);
                reminder.remind(settings);
            }
        }
    }
}
