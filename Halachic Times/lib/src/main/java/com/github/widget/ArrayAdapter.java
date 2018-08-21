/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.widget;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.util.LogUtils;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Array adapter ported from {@link android.widget.ArrayAdapter} to {@link RecyclerView}.
 *
 * @author Moshe Waisberg
 */
public class ArrayAdapter<T, VH extends ArrayAdapter.ArrayViewHolder> extends RecyclerView.Adapter<VH> implements Filterable {

    /**
     * Lock used to modify the content of {@link #objects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    protected final Object lock = new Object();

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    @LayoutRes
    private final int layoutResource;

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    protected final List<T> objects = new ArrayList<>();

    /**
     * If the inflated resource is not a TextView, {@code textFieldId} is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    @IdRes
    private int textFieldId = 0;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #objects} is modified.
     */
    private boolean notifyOnChange = true;

    /**
     * A copy of the original objects array, initialized from and then used instead as soon as
     * the filter is used. objects will then only contain the filtered values.
     */
    protected final List<T> originalValues = new ArrayList<>();
    protected boolean objectsFiltered;

    private ArrayFilter filter;

    /**
     * Constructor
     *
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    public ArrayAdapter(@LayoutRes int resource) {
        this(resource, 0);
    }

    /**
     * Constructor
     *
     * @param resource           The resource ID for a layout file containing a layout to use when
     *                           instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     */
    public ArrayAdapter(@LayoutRes int resource, @IdRes int textViewResourceId) {
        this(resource, textViewResourceId, new ArrayList<T>());
    }

    /**
     * Constructor
     *
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @NonNull T[] objects) {
        this(resource, 0, Arrays.asList(objects));
    }

    /**
     * Constructor
     *
     * @param resource           The resource ID for a layout file containing a layout to use when
     *                           instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects            The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @IdRes int textViewResourceId, @NonNull T[] objects) {
        this(resource, textViewResourceId, Arrays.asList(objects));
    }

    /**
     * Constructor
     *
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @NonNull List<T> objects) {
        this(resource, 0, objects);
    }

    /**
     * Constructor
     *
     * @param resource           The resource ID for a layout file containing a layout to use when
     *                           instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects            The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<T> objects) {
        this.layoutResource = resource;
        this.objects.addAll(objects);
        this.textFieldId = textViewResourceId;
        setHasStableIds(true);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(@Nullable T object) {
        int position = 0;
        synchronized (lock) {
            if (objectsFiltered) {
                position = originalValues.size();
                originalValues.add(object);
            } else {
                position = objects.size();
                objects.add(object);
            }
        }
        if (notifyOnChange) notifyItemInserted(position);
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this list
     * @throws NullPointerException          if the specified collection contains one
     *                                       or more null elements and this list does not permit null
     *                                       elements, or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this list
     */
    public void addAll(@NonNull Collection<? extends T> collection) {
        int position = 0;
        int count = collection.size();
        synchronized (lock) {
            if (objectsFiltered) {
                position = originalValues.size();
                originalValues.addAll(collection);
            } else {
                position = objects.size();
                objects.addAll(collection);
            }
        }
        if (notifyOnChange) notifyItemRangeInserted(position, count);
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T... items) {
        int position = 0;
        int count = items.length;
        synchronized (lock) {
            if (objectsFiltered) {
                position = originalValues.size();
                Collections.addAll(originalValues, items);
            } else {
                position = objects.size();
                Collections.addAll(objects, items);
            }
        }
        if (notifyOnChange) notifyItemRangeInserted(position, count);
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insert(@Nullable T object, int index) {
        synchronized (lock) {
            if (objectsFiltered) {
                originalValues.add(index, object);
            } else {
                objects.add(index, object);
            }
        }
        if (notifyOnChange) notifyItemInserted(index);
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(@Nullable T object) {
        int position;
        synchronized (lock) {
            if (objectsFiltered) {
                position = originalValues.indexOf(object);
                originalValues.remove(position);
            } else {
                position = objects.indexOf(object);
                objects.remove(position);
            }
        }
        if (notifyOnChange) notifyItemRemoved(position);
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (lock) {
            if (objectsFiltered) {
                originalValues.clear();
            } else {
                objects.clear();
            }
        }
        if (notifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *                   in this adapter.
     */
    public void sort(@NonNull Comparator<? super T> comparator) {
        int count = 0;
        synchronized (lock) {
            if (objectsFiltered) {
                count = originalValues.size();
                Collections.sort(originalValues, comparator);
            } else {
                count = objects.size();
                Collections.sort(objects, comparator);
            }
        }
        if (notifyOnChange) notifyItemRangeChanged(0, count);
    }

    /**
     * Control whether methods that change the list ({@link #add}, {@link #addAll(Collection)},
     * {@link #addAll(Object[])}, {@link #insert}, {@link #remove}, {@link #clear},
     * {@link #sort(Comparator)}) automatically call {@link #notifyDataSetChanged}.  If set to
     * false, caller must manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     * <p>
     * The default is true, and calling notifyDataSetChanged() resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        this.notifyOnChange = notifyOnChange;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Nullable
    public T getItem(int position) {
        return objects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(@Nullable T item) {
        return objects.indexOf(item);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
        return createArrayViewHolder(view, textFieldId);
    }

    protected VH createArrayViewHolder(View view, int fieldId) {
        return (VH) new ArrayViewHolder(view, fieldId);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    @NonNull
    public Filter getFilter() {
        if (filter == null) {
            filter = createFilter();
        }
        return filter;
    }

    protected ArrayFilter createFilter() {
        return new ArrayFilter();
    }

    public static class ArrayViewHolder<T> extends RecyclerView.ViewHolder {

        protected final TextView textView;

        public ArrayViewHolder(View itemView, int fieldId) {
            super(itemView);

            try {
                textView = (TextView) ((fieldId == 0) ? itemView : itemView.findViewById(fieldId));
            } catch (ClassCastException e) {
                LogUtils.e("ArrayAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException(
                        "ArrayAdapter requires the resource ID to be a TextView", e);
            }
        }

        public void bind(T item) {
            if (item instanceof CharSequence) {
                textView.setText((CharSequence) item);
            } else {
                textView.setText(item.toString());
            }
        }
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    protected class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();

            if (!objectsFiltered) {
                synchronized (lock) {
                    objectsFiltered = true;
                    originalValues.clear();
                    originalValues.addAll(objects);
                }
            }

            if (TextUtils.isEmpty(prefix)) {
                final List<T> list;
                synchronized (lock) {
                    list = new ArrayList<>(originalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                final List<T> values;
                synchronized (lock) {
                    values = new ArrayList<>(originalValues);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            synchronized (lock) {
                objects.clear();
                if (results.count > 0) {
                    objects.addAll((List<T>) results.values);
                }
                notifyDataSetChanged();
                notifyOnChange = true;
            }
        }
    }

    /**
     * Permanently removes the specified object from the array.
     *
     * @param object The object to delete.
     */
    public void delete(@Nullable T object) {
        int position;
        synchronized (lock) {
            position = originalValues.indexOf(object);
            originalValues.remove(position);

            position = objects.indexOf(object);
            if (position >= 0) {
                objects.remove(position);
            }
        }
        if (notifyOnChange) notifyItemRemoved(position);
    }
}
