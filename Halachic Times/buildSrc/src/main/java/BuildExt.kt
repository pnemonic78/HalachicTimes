fun Iterable<String>.toJavaString(): String {
    return "{\"" + joinToString("\", \"") + "\"}"
}
