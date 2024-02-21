/*
 * Copyright 2018, Moshe Waisberg
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
package com.github.io

import java.io.ByteArrayOutputStream

/**
 * Byte array output stream with raw access to the byte buffer.
 *
 * @author moshe on 2018/04/24.
 */
class RawByteArrayOutputStream : ByteArrayOutputStream {

    constructor() : super()

    constructor(size: Int) : super(size)

    val byteArray: ByteArray
        get() = buf
}