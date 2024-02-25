package com.github.geonames

import com.github.geonames.dump.NameCities1000
import com.github.geonames.dump.PathCities1000
import java.io.File

class AllCities : Cities() {
    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun main(args: Array<String>) {
            val path = PathCities1000
            val res = File(path)
            val cities = Cities()
            val names = cities.loadNames(res, CityFilter(), NameCities1000)
            val capitals = cities.filterCapitals(names)
            cities.writeAndroidXML(capitals, null)
        }
    }
}