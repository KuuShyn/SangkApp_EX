package com.thesis.sangkapp_ex

import android.graphics.Bitmap

object DepthExtractor {

    /**
     * Extracts normalized depth values from an ALPHA_8 Bitmap.
     *
     * @param depthMap The Bitmap containing the depth map.
     * @return A 2D array of normalized depth values between 0.0 and 1.0.
     */
    fun extractNormalizedDepth(depthMap: Bitmap): Array<FloatArray> {
        val width = depthMap.width
        val height = depthMap.height
        val depthValues = Array(height) { FloatArray(width) }

        // Allocate an array to hold the alpha values
        val pixels = IntArray(width * height)
        depthMap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Iterate over each pixel and extract the alpha value
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val alpha = pixels[index] and 0xFF // Extract alpha (0-255)
                // Normalize alpha to a float between 0.0 and 1.0
                depthValues[y][x] = alpha / 255.0f
            }
        }

        return depthValues
    }

    /**
     * Maps normalized depth values to actual depth measurements.
     *
     * @param normalizedDepth A 2D array of normalized depth values (0.0 - 1.0).
     * @param minDepth The minimum depth value (e.g., 0.5 meters).
     * @param maxDepth The maximum depth value (e.g., 10.0 meters).
     * @return A 2D array of actual depth measurements.
     */
    fun mapNormalizedDepth(
        normalizedDepth: Array<FloatArray>,
        minDepth: Float,
        maxDepth: Float
    ): Array<FloatArray> {
        val height = normalizedDepth.size
        val width = normalizedDepth[0].size
        val actualDepth = Array(height) { FloatArray(width) }

        for (y in 0 until height) {
            for (x in 0 until width) {
                actualDepth[y][x] = minDepth + normalizedDepth[y][x] * (maxDepth - minDepth)
            }
        }

        return actualDepth
    }

    /**
     * Converts a 2D array of depth values to a flat FloatArray.
     *
     * @param depth2D The 2D array of depth values.
     * @return A flat FloatArray containing all depth values.
     */
    fun flattenDepthValues(depth2D: Array<FloatArray>): FloatArray {
        val height = depth2D.size
        val width = depth2D[0].size
        val flatDepth = FloatArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                flatDepth[y * width + x] = depth2D[y][x]
            }
        }
        return flatDepth
    }
}
