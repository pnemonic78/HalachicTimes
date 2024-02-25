package com.github.json

import kotlinx.serialization.json.Json

val JsonIgnore: Json get() = Json { ignoreUnknownKeys = true }
