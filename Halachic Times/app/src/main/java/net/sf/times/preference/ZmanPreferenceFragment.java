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

    protected static final String EXTRA_XML = "xml";
    protected static final String EXTRA_OPINION = "opinion";

    private int xmlId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String xmlName = args.getString(EXTRA_XML);
        Resources res = getResources();
        this.xmlId = res.getIdentifier(xmlName, "xml", getActivity().getPackageName());
        String opinion = args.getString(EXTRA_OPINION);

        super.onCreate(savedInstanceState);

        initList(opinion);
    }

    @Override
    protected int getPreferencesXml() {
        return xmlId;
    }
}
