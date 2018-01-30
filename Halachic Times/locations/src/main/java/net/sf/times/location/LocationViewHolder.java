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
class LocationViewHolder extends ArrayAdapter.ArrayViewHolder<LocationAdapter.LocationItem> implements View.OnClickListener {

    protected final TextView cityName;
    protected final TextView coordinates;
    protected final CheckBox favorite;

    protected LocationAdapter.LocationItem item;

    private final LocationAdapter.OnItemClickListener itemClickListener;
    private final LocationAdapter.OnFavoriteClickListener favoriteClickListener;

    public LocationViewHolder(View itemView, int fieldId, LocationAdapter.OnItemClickListener itemClickListener, LocationAdapter.OnFavoriteClickListener favoriteClickListener) {
        super(itemView, fieldId);

        this.cityName = textView;
        this.coordinates = itemView.findViewById(R.id.coordinates);
        this.favorite = itemView.findViewById(android.R.id.checkbox);

        this.itemClickListener = itemClickListener;
        this.favoriteClickListener = favoriteClickListener;
        itemView.setOnClickListener(this);
        favorite.setOnClickListener(this);
    }

    @Override
    public void bind(LocationAdapter.LocationItem item) {
        this.item = item;

        cityName.setText(item.getLabel());
        coordinates.setText(item.getCoordinates());
        favorite.setChecked(item.isFavorite());
        favorite.setTag(item.getAddress());
    }

    public LocationAdapter.LocationItem getItem() {
        return item;
    }

    @Override
    public void onClick(View view) {
        if ((view == favorite) && (favoriteClickListener != null)) {
            favoriteClickListener.onFavoriteClick(item.getAddress(), favorite.isChecked());
        } else if (itemClickListener != null) {
            itemClickListener.onItemClick(item.getAddress());
        }
    }
}
