/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sproj.arimagerecognizer.arhelper.ml.classification

import android.app.Activity
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.sproj.arimagerecognizer.arhelper.ml.classification.utils.ImageUtils
import com.sproj.arimagerecognizer.arhelper.ml.classification.utils.VertexUtils.rotateCoordinates
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import kotlinx.coroutines.tasks.asDeferred
import kotlin.math.max
import kotlin.math.min

/**
 * Analyzes an image using ML Kit.
 */
class MLKitObjectDetector(context: Activity) : ObjectDetector(context) {
    // To use a custom model, follow steps on https://developers.google.com/ml-kit/vision/object-detection/custom-models/android.
    val model = LocalModel.Builder().setAssetFilePath("12.tflite").build()
    val builder = CustomObjectDetectorOptions.Builder(model)

    // For the ML Kit default model, use the following:
//  val builder = ObjectDetectorOptions.Builder()

    private val options = builder
        .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableClassification()
        .enableMultipleObjects()
        .build()
    private val detector = ObjectDetection.getClient(options)

    object DetectedObjectsRepository {
        val detectedObjects = mutableListOf<DetectedObjectResult>()
    }


    override suspend fun analyze(
        image: Image,
        imageRotation: Int
    ): Pair<List<DetectedObjectResult>, List<Bitmap>> {
        // `image` is in YUV (https://developers.google.com/ar/reference/java/com/google/ar/core/Frame#acquireCameraImage()),
        val convertYuv = convertYuv(image)

        // The model performs best on upright images, so rotate it.
        val rotatedImage = ImageUtils.rotateBitmap(convertYuv, imageRotation)

        val inputImage = InputImage.fromBitmap(rotatedImage, 0)

        val mlKitDetectedObjects = detector.process(inputImage).asDeferred().await()

        val detectedObjects = mlKitDetectedObjects.mapNotNull { obj ->
            val bestLabel =
                obj.labels.maxByOrNull { label -> label.confidence } ?: return@mapNotNull null
            Log.d("Object detection", bestLabel.text)
            val coords =
                obj.boundingBox.exactCenterX().toInt() to obj.boundingBox.exactCenterY().toInt()
            val rotatedCoordinates =
                coords.rotateCoordinates(rotatedImage.width, rotatedImage.height, imageRotation)
            DetectedObjectResult(bestLabel.confidence, bestLabel.text, rotatedCoordinates)
        }

        val croppedImages = mlKitDetectedObjects.mapNotNull { obj ->
            val boundingBox = obj.boundingBox
            // Ensure the bounding box coordinates are within bounds
            val left = max(0, boundingBox.left)
            val top = max(0, boundingBox.top)
            val right = min(rotatedImage.width, boundingBox.right)
            val bottom = min(rotatedImage.height, boundingBox.bottom)

            // Crop the original image using the bounding box coordinates
            val croppedBitmap =
                Bitmap.createBitmap(rotatedImage, left, top, right - left, bottom - top)

            croppedBitmap
        }

        return Pair(detectedObjects, croppedImages)
    }


    @Suppress("USELESS_IS_CHECK")
    fun hasCustomModel() = builder is CustomObjectDetectorOptions.Builder
}