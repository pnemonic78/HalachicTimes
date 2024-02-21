package com.github.geonames

import org.geonames.FeatureClass

/**
 * Features.
 */
object Features {
    /**
     * Feature class for a populated city.
     */
    @JvmField
    val FEATURE_P = FeatureClass.P.name

    /**
     * Feature code for a populated place.
     */
    const val FEATURE_PPL = "PPL"

    /**
     * Feature code for a seat of a first-order administrative division.
     */
    const val FEATURE_PPLA = "PPLA"

    /**
     * Feature code for a seat of a second-order administrative division.
     */
    const val FEATURE_PPLA2 = "PPLA2"

    /**
     * Feature code for a seat of a third-order administrative division.
     */
    const val FEATURE_PPLA3 = "PPLA3"

    /**
     * Feature code for a seat of a fourth-order administrative division.
     */
    const val FEATURE_PPLA4 = "PPLA4"

    /**
     * Feature code for a capital of a political entity.
     */
    const val FEATURE_PPLC = "PPLC"

    /**
     * Feature code for a farm village.
     */
    const val FEATURE_PPLF = "PPLF"

    /**
     * Feature code for a seat of government of a political entity.
     */
    const val FEATURE_PPLG = "PPLG"

    /**
     * Feature code for a populated locality.
     */
    const val FEATURE_PPLL = "PPLL"

    /**
     * Feature code for an abandoned populated place.
     */
    const val FEATURE_PPLQ = "PPLQ"

    /**
     * Feature code for a religious populated place.
     */
    const val FEATURE_PPLR = "PPLR"

    /**
     * Feature code for populated places.
     */
    const val FEATURE_PPLS = "PPLS"

    /**
     * Feature code for a destroyed populated place.
     */
    const val FEATURE_PPLW = "PPLW"

    /**
     * Feature code for a section of populated place.
     */
    const val FEATURE_PPLX = "PPLX"

    /**
     * Feature code for an Israeli settlement.
     */
    const val FEATURE_STLMT = "STLMT"
}
