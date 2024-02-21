package com.github.geonames

import java.io.File

class AllCities : Cities() {
    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun main(args: Array<String>) {
            val path = "GeoNames/res/cities1000.txt"
            val res = File(path)
            println(res)
            println(res.absolutePath)
            val cities = Cities()
            val names = cities.loadNames(res, CityFilter())
            val capitals = cities.filterCapitals(names)
            cities.writeAndroidXML(capitals, null)
        }
    }
}