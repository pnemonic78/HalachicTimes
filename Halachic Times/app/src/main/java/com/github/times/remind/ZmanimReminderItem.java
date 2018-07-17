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
package com.github.times.remind;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import static com.github.times.ZmanimItem.NEVER;

/**
 * Reminder item for a notification.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderItem implements Parcelable {

    public final int id;
    public final CharSequence title;
    public final CharSequence text;
    public final long time;

    public ZmanimReminderItem(int id, @NonNull CharSequence title, CharSequence text, long time) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.time = time;
    }

    protected ZmanimReminderItem(Parcel in) {
        id = in.readInt();
        title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        text = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        time = in.readLong();
    }

    public boolean isEmpty() {
        return (id == 0) || (time == NEVER) || (title == null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        TextUtils.writeToParcel(title, parcel, flags);
        TextUtils.writeToParcel(text, parcel, flags);
        parcel.writeLong(time);
    }

    public static final Creator<ZmanimReminderItem> CREATOR = new Creator<ZmanimReminderItem>() {
        @Override
        public ZmanimReminderItem createFromParcel(Parcel in) {
            return new ZmanimReminderItem(in);
        }

        @Override
        public ZmanimReminderItem[] newArray(int size) {
            return new ZmanimReminderItem[size];
        }
    };
}
