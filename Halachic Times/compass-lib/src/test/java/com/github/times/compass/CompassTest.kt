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
package com.github.times.compass

import android.hardware.SensorManager
import com.github.times.BaseTests
import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test compass calculations.
 *
 * @author Moshe Waisberg
 */
class CompassTest : BaseTests() {
    @Test
    fun rotationMatrixPortrait0() {
        val gravity = floatArrayOf(0.0f, 9.8f, 0.0f)
        val geomagnetic = floatArrayOf(22.0f, 5.90f, -43.10f)
        val matrixRExpected = floatArrayOf(
            0.8906765f,
            -0.0f,
            0.45463768f,
            0.45463768f,
            0.0f,
            -0.8906765f,
            0.0f,
            1.0f,
            0.0f
        )

        val matrixR = FloatArray(9)
        assertTrue(SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic))
        assertEquals(matrixRExpected[0], matrixR[0])
        assertEquals(matrixRExpected[1], matrixR[1])
        assertEquals(matrixRExpected[2], matrixR[2])
        assertEquals(matrixRExpected[3], matrixR[3])
        assertEquals(matrixRExpected[4], matrixR[4])
        assertEquals(matrixRExpected[5], matrixR[5])
        assertEquals(matrixRExpected[6], matrixR[6])
        assertEquals(matrixRExpected[7], matrixR[7])
        assertEquals(matrixRExpected[8], matrixR[8])

        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrixR, orientation)
        assertEquals(-0.0f, orientation[0])
        assertEquals((PI * -0.5).toFloat(), orientation[1])
        assertEquals(-0.0f, orientation[2])
    }

    @Test
    fun rotationMatrixLandscape270() {
        val gravity = floatArrayOf(9.8f, 0.0f, 0.0f)
        val geomagnetic = floatArrayOf(5.90f, -22.0f, -43.10f)
        val matrixRExpected = floatArrayOf(
            0.0f,
            -0.8906765f,
            0.45463768f,
            0.0f,
            -0.45463768f,
            -0.8906765f,
            1.0f,
            0.0f,
            0.0f
        )

        val matrixR = FloatArray(9)
        assertTrue(SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic))
        assertEquals(matrixRExpected[0], matrixR[0])
        assertEquals(matrixRExpected[1], matrixR[1])
        assertEquals(matrixRExpected[2], matrixR[2])
        assertEquals(matrixRExpected[3], matrixR[3])
        assertEquals(matrixRExpected[4], matrixR[4])
        assertEquals(matrixRExpected[5], matrixR[5])
        assertEquals(matrixRExpected[6], matrixR[6])
        assertEquals(matrixRExpected[7], matrixR[7])
        assertEquals(matrixRExpected[8], matrixR[8])

        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrixR, orientation)
        assertEquals(-2.0427618f, orientation[0])
        assertEquals(-0.0f, orientation[1])
        assertEquals((PI * -0.5).toFloat(), orientation[2])
    }

    @Test
    fun rotationMatrixPortrait0_LG() {
        val gravity = floatArrayOf(-0.10595338f, -0.528306f, 9.604765f)
        val geomagnetic = floatArrayOf(26.638561f, -13.187685f, 29.62915f)
        val matrixRExpected = floatArrayOf(
            -0.3933655f,
            -0.9177463f,
            -0.05481959f,
            0.91931605f,
            -0.39335173f,
            -0.011494861f,
            -0.011014016f,
            -0.054918222f,
            0.9984301f
        )
        val orientationExpected = floatArrayOf(-1.9757174f, 0.054945864f, 0.011030887f)

        val matrixR = FloatArray(9)
        assertTrue(SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic))
        assertEquals(matrixRExpected[0], matrixR[0])
        assertEquals(matrixRExpected[1], matrixR[1])
        assertEquals(matrixRExpected[2], matrixR[2])
        assertEquals(matrixRExpected[3], matrixR[3])
        assertEquals(matrixRExpected[4], matrixR[4])
        assertEquals(matrixRExpected[5], matrixR[5])
        assertEquals(matrixRExpected[6], matrixR[6])
        assertEquals(matrixRExpected[7], matrixR[7])
        assertEquals(matrixRExpected[8], matrixR[8])

        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrixR, orientation)
        assertEquals(orientationExpected[0], orientation[0])
        assertEquals(orientationExpected[1], orientation[1])
        assertEquals(orientationExpected[2], orientation[2])

        val mapR = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            matrixR,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Y,
            mapR
        )
        assertEquals(matrixRExpected[0], mapR[0])
        assertEquals(matrixRExpected[1], mapR[1])
        assertEquals(matrixRExpected[2], mapR[2])
        assertEquals(matrixRExpected[3], mapR[3])
        assertEquals(matrixRExpected[4], mapR[4])
        assertEquals(matrixRExpected[5], mapR[5])
        assertEquals(matrixRExpected[6], mapR[6])
        assertEquals(matrixRExpected[7], mapR[7])
        assertEquals(matrixRExpected[8], mapR[8])
        SensorManager.getOrientation(mapR, orientation)
        assertEquals(orientationExpected[0], orientation[0])
        assertEquals(orientationExpected[1], orientation[1])
        assertEquals(orientationExpected[2], orientation[2])
    }

    @Test
    fun rotationMatrixLandscape90_LG() {
        val gravity = floatArrayOf(-0.122543335f, -0.55407715f, 9.598358f)
        val geomagnetic = floatArrayOf(40.792274f, -22.970963f, 13.322449f)
        val matrixRExpected = floatArrayOf(
            -0.47574702f,
            -0.8777499f,
            -0.056743126f,
            0.8794898f,
            -0.47564095f,
            -0.016228417f,
            -0.012744861f,
            -0.05762562f,
            0.9982569f
        )
        val mapRExpected = floatArrayOf(
            0.8777499f,
            -0.47574702f,
            -0.056743126f,
            0.47564095f,
            0.8794898f,
            -0.016228417f,
            0.05762562f,
            -0.012744861f,
            0.9982569f
        )
        val orientationExpected = floatArrayOf(-2.0673892f, 0.057657562f, 0.012766421f)
        val orientationMappedExpected = floatArrayOf(-0.4958571f, 0.012745205f, -0.05766225f)

        val matrixR = FloatArray(9)
        assertTrue(SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic))
        assertEquals(matrixRExpected[0], matrixR[0])
        assertEquals(matrixRExpected[1], matrixR[1])
        assertEquals(matrixRExpected[2], matrixR[2])
        assertEquals(matrixRExpected[3], matrixR[3])
        assertEquals(matrixRExpected[4], matrixR[4])
        assertEquals(matrixRExpected[5], matrixR[5])
        assertEquals(matrixRExpected[6], matrixR[6])
        assertEquals(matrixRExpected[7], matrixR[7])
        assertEquals(matrixRExpected[8], matrixR[8])

        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrixR, orientation)
        assertEquals(orientationExpected[0], orientation[0])
        assertEquals(orientationExpected[1], orientation[1])
        assertEquals(orientationExpected[2], orientation[2])

        val mapR = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            matrixR,
            SensorManager.AXIS_Y,
            SensorManager.AXIS_MINUS_X,
            mapR
        )
        assertEquals(mapRExpected[0], mapR[0])
        assertEquals(mapRExpected[1], mapR[1])
        assertEquals(mapRExpected[2], mapR[2])
        assertEquals(mapRExpected[3], mapR[3])
        assertEquals(mapRExpected[4], mapR[4])
        assertEquals(mapRExpected[5], mapR[5])
        assertEquals(mapRExpected[6], mapR[6])
        assertEquals(mapRExpected[7], mapR[7])
        assertEquals(mapRExpected[8], mapR[8])

        SensorManager.getOrientation(mapR, orientation)
        assertEquals(orientationMappedExpected[0], orientation[0])
        assertEquals(orientationMappedExpected[1], orientation[1])
        assertEquals(orientationMappedExpected[2], orientation[2])
    }

    @Test
    fun rotationMatrixLandscape180_LG() {
        val gravity = floatArrayOf(-0.14660645f, -0.5656891f, 9.577408f)
        val geomagnetic = floatArrayOf(22.807312f, -37.033463f, 53.443718f)
        val matrixRExpected = floatArrayOf(
            -0.81935644f,
            -0.5714123f,
            -0.046292763f,
            0.5730808f,
            -0.8185428f,
            -0.039574735f,
            -0.015279113f,
            -0.058955304f,
            0.9981437f
        )
        val mapRExpected = floatArrayOf(
            -0.81935644f,
            0.5714123f,
            0.046292763f,
            0.5730808f,
            0.8185428f,
            0.039574735f,
            -0.015279113f,
            0.058955304f,
            -0.9981437f
        )
        val orientationExpected = floatArrayOf(-2.5321531f, 0.05898951f, 0.015306334f)
        val orientationMappedExpected = floatArrayOf(0.6094395f, -0.05898951f, 3.1262863f)

        val matrixR = FloatArray(9)
        assertTrue(SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic))
        assertEquals(matrixRExpected[0], matrixR[0])
        assertEquals(matrixRExpected[1], matrixR[1])
        assertEquals(matrixRExpected[2], matrixR[2])
        assertEquals(matrixRExpected[3], matrixR[3])
        assertEquals(matrixRExpected[4], matrixR[4])
        assertEquals(matrixRExpected[5], matrixR[5])
        assertEquals(matrixRExpected[6], matrixR[6])
        assertEquals(matrixRExpected[7], matrixR[7])
        assertEquals(matrixRExpected[8], matrixR[8])

        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrixR, orientation)
        assertEquals(orientationExpected[0], orientation[0])
        assertEquals(orientationExpected[1], orientation[1])
        assertEquals(orientationExpected[2], orientation[2])

        val mapR = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            matrixR,
            SensorManager.AXIS_X,
            SensorManager.AXIS_MINUS_Y,
            mapR
        )
        assertEquals(mapRExpected[0], mapR[0])
        assertEquals(mapRExpected[1], mapR[1])
        assertEquals(mapRExpected[2], mapR[2])
        assertEquals(mapRExpected[3], mapR[3])
        assertEquals(mapRExpected[4], mapR[4])
        assertEquals(mapRExpected[5], mapR[5])
        assertEquals(mapRExpected[6], mapR[6])
        assertEquals(mapRExpected[7], mapR[7])
        assertEquals(mapRExpected[8], mapR[8])

        SensorManager.getOrientation(mapR, orientation)
        assertEquals(orientationMappedExpected[0], orientation[0])
        assertEquals(orientationMappedExpected[1], orientation[1])
        assertEquals(orientationMappedExpected[2], orientation[2])
    }

    @Test
    fun rotationMatrixLandscape270_LG() {
        val gravity = floatArrayOf(-0.15977478f, -0.56747437f, 9.59285f)
        val geomagnetic = floatArrayOf(47.165108f, -17.170334f, 18.540382f)
        val matrixRExpected = floatArrayOf(
            -0.32009178f,
            -0.94540405f,
            -0.061257623f,
            0.9472406f,
            -0.32050735f,
            -0.0031830538f,
            -0.016624246f,
            -0.059044573f,
            0.9981168f
        )
        val mapRExpected = floatArrayOf(
            -0.94540405f,
            0.32009178f,
            -0.061257623f,
            -0.32050735f,
            -0.9472406f,
            -0.0031830538f,
            -0.059044573f,
            0.016624246f,
            0.9981168f
        )
        val orientationExpected = floatArrayOf(-1.8976527f, 0.059078936f, 0.016654072f)
        val orientationMappedExpected = floatArrayOf(2.8157196f, -0.016625011f, 0.059087116f)

        val matrixR = FloatArray(9)
        assertTrue(SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic))
        assertEquals(matrixRExpected[0], matrixR[0])
        assertEquals(matrixRExpected[1], matrixR[1])
        assertEquals(matrixRExpected[2], matrixR[2])
        assertEquals(matrixRExpected[3], matrixR[3])
        assertEquals(matrixRExpected[4], matrixR[4])
        assertEquals(matrixRExpected[5], matrixR[5])
        assertEquals(matrixRExpected[6], matrixR[6])
        assertEquals(matrixRExpected[7], matrixR[7])
        assertEquals(matrixRExpected[8], matrixR[8])

        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrixR, orientation)
        assertEquals(orientationExpected[0], orientation[0])
        assertEquals(orientationExpected[1], orientation[1])
        assertEquals(orientationExpected[2], orientation[2])

        val mapR = FloatArray(9)
        SensorManager.remapCoordinateSystem(
            matrixR,
            SensorManager.AXIS_MINUS_Y,
            SensorManager.AXIS_X,
            mapR
        )
        assertEquals(mapRExpected[0], mapR[0])
        assertEquals(mapRExpected[1], mapR[1])
        assertEquals(mapRExpected[2], mapR[2])
        assertEquals(mapRExpected[3], mapR[3])
        assertEquals(mapRExpected[4], mapR[4])
        assertEquals(mapRExpected[5], mapR[5])
        assertEquals(mapRExpected[6], mapR[6])
        assertEquals(mapRExpected[7], mapR[7])
        assertEquals(mapRExpected[8], mapR[8])

        SensorManager.getOrientation(mapR, orientation)
        assertEquals(orientationMappedExpected[0], orientation[0])
        assertEquals(orientationMappedExpected[1], orientation[1])
        assertEquals(orientationMappedExpected[2], orientation[2])
    }
}