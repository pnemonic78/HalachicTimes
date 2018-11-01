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
package com.github.times.location.google;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.maps.model.AddressComponentType;

import java.io.IOException;

/**
 * Address component type JSON type adapter.
 *
 * @author Moshe Waisberg
 */
class AddressComponentTypeAdapter extends TypeAdapter<AddressComponentType> {

    @Override
    public AddressComponentType read(JsonReader in) throws IOException {
        final String value = in.nextString();
        AddressComponentType[] types = AddressComponentType.values();
        for (AddressComponentType type : types) {
            if (value.equals(type.toCanonicalLiteral())) {
                return type;
            }
        }
        return null;
    }

    @Override
    public void write(JsonWriter out, AddressComponentType value) throws IOException {
        out.value(out.toString());
    }
}
