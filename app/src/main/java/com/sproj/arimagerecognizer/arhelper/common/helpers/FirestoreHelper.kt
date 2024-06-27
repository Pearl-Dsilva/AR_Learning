package com.sproj.arimagerecognizer.arhelper.common.helpers

import android.graphics.Bitmap
import android.util.Log
import com.sproj.arimagerecognizer.arhelper.ml.classification.DetectedObjectResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicInteger

class FirestoreHelper {
    private val TAG = "FirestoreHelper"
    suspend fun sendLabelsToFirestore(detectedObjects: Pair<List<DetectedObjectResult>, List<Bitmap>>?) {
        // Switch to the IO dispatcher for performing network operations
        withContext(Dispatchers.IO) {
            // Initialize Firestore
            val db = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            val user = FirebaseAuth.getInstance()

            // Create a batch object for efficient writes
            val batch = db.batch()
            val tasksLeft = AtomicInteger()
            val allTasksCompleted = CompletableDeferred<Unit>()

            detectedObjects?.second?.forEachIndexed { index, bitmap ->
                // Generate a unique filename for the image
                val filename = "${detectedObjects.first[index].label}.jpg"

                // Create a reference to the Firebase Storage location
                val imageRef =
                    storage.reference.child("images/${user.currentUser?.email}/$filename")

                // Convert bitmap to byte array
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                tasksLeft.getAndIncrement()

                // Upload image to Firebase Storage
                imageRef.putBytes(data)
                    .addOnSuccessListener { _ ->
                        Log.d(TAG, "$filename Pushed to Storage")
                        val documentRef =
                            db.collection("labels/${user.currentUser?.email}/recognisedLabels")
                                .document(detectedObjects.first[index].label)

                        // Set the data for the document
                        batch.set(
                            documentRef,
                            mapOf(
                                "label" to detectedObjects.first[index].label,
                                "timestamp" to Timestamp.now(),
                                "completed" to false
                            )
                        )

                        if (tasksLeft.decrementAndGet() == 0) {
                            // If all tasks are completed, resolve the deferred object
                            allTasksCompleted.complete(Unit)
                        }
                    }
                    .addOnFailureListener { e ->
                        println("$filename was not Pushed to Storage")
                        if (tasksLeft.decrementAndGet() == 0) {
                            // If all tasks are completed, resolve the deferred object
                            allTasksCompleted.complete(Unit)
                        }
                    }
            }

            // Wait for all tasks to complete
            allTasksCompleted.await()

            // Commit the batch operation
            batch.commit()
                .addOnSuccessListener {
                    println("Labels sent to Firestore successfully.")
                }
                .addOnFailureListener { e ->
                    println("Error sending labels to Firestore: $e")
                }
        }

    }
}
