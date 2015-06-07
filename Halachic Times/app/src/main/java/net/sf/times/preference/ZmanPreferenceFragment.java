package net.sf.times.preference;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

/**
 * This fragment shows the preferences for a zman screen.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ZmanPreferenceFragment extends AbstractPreferenceFragment {

    public static final String EXTRA_XML = "xml";
    public static final String EXTRA_OPINION = "opinion";
    public static final String EXTRA_REMINDER = "reminder";

    private int xmlId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String xmlName = args.getString(EXTRA_XML);
        xmlName = xmlName.replace("res/", "").replace(".xml", "");
        Resources res = getResources();
        this.xmlId = res.getIdentifier(xmlName, null, getActivity().getPackageName());
        String opinion = args.getString(EXTRA_OPINION);
        String reminder = args.getString(EXTRA_REMINDER);

        super.onCreate(savedInstanceState);

        initList(opinion);
        initList(reminder);
    }

    @Override
    protected int getPreferencesXml() {
        return xmlId;
    }
}
