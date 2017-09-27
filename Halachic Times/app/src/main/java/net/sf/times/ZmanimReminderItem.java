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
package net.sf.times;

/**
 * Reminder item for a notification.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminderItem {

    public final int id;
    public final CharSequence title;
    public final CharSequence text;
    public final long time;

    public ZmanimReminderItem(int id, CharSequence title, CharSequence text, long time) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.time = time;
    }
}
