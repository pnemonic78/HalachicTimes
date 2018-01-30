package net.sf.times.location;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import net.sf.widget.ArrayAdapter;

/**
 * View holder for location row item.
 *
 * @author Moshe Waisberg
 */
class LocationViewHolder extends ArrayAdapter.ArrayViewHolder<LocationAdapter.LocationItem> {

    public final TextView cityName;
    public final TextView coordinates;
    public final CheckBox favorite;

    public LocationViewHolder(View itemView, int fieldId) {
        super(itemView, fieldId);

        this.cityName = textView;
        this.coordinates = itemView.findViewById(R.id.coordinates);
        this.favorite = itemView.findViewById(android.R.id.checkbox);
    }

    @Override
    public void bind(LocationAdapter.LocationItem item) {
        cityName.setText(item.getLabel());
        coordinates.setText(item.getCoordinates());
        favorite.setChecked(item.isFavorite());
        favorite.setTag(item.getAddress());
    }
}
