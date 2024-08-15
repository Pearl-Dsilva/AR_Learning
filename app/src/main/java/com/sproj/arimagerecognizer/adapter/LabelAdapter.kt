package com.sproj.arimagerecognizer.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sproj.arimagerecognizer.LanguageManager
import com.sproj.arimagerecognizer.R
import com.sproj.arimagerecognizer.adapter.LabelAdapter.LabelViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

enum class State {
    STARTED,
    RUNNING,


}


class LabelAdapter(
    private val context: Context,
    sharedPreferences: SharedPreferences,

    ) : RecyclerView.Adapter<LabelViewHolder>() {

    private var labels: List<Pair<String, Date>>
    private lateinit var translations: MutableList<String>
    private var options: TranslatorOptions
    private var languageTranslator: Translator
    private var loadedModel: AtomicBoolean
    private var activeCoroutinesCount = 0

    init {
        labels = ArrayList()
        val languageManager = LanguageManager(sharedPreferences)
        val languagesArray = ArrayList(LanguageManager.availableLanguages.keys)

        var targetLanguage =
            languageManager.availableLanguage.get(languagesArray[languageManager.language])
        if (targetLanguage == null) {
            targetLanguage = TranslateLanguage.ENGLISH
        }
        options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguage)
            .build()
        Log.d(TAG, "language: ${languagesArray.get(languageManager.language)}")
        languageTranslator = Translation.getClient(options)
        loadedModel = AtomicBoolean(false)
    }

    fun updateData(labels: List<Pair<String, Date>>) {
        this.labels = labels
        this.translations = MutableList(labels.size) { "" }
        notifyDataSetChanged()
    }

    private suspend fun downloadModelIfNeeded(conditions: DownloadConditions) {

        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Will Download Model if not Available", Toast.LENGTH_LONG)
                    .show()
            }

            languageTranslator.downloadModelIfNeeded(conditions).await()
        } catch (exception: Exception) {
            loadedModel.set(false)
            Log.e(TAG, "Error downloading translation model: ", exception)
            throw exception // Re-throw the exception for handling in the caller
        }
    }

    private suspend fun translateText(text: String): String {
        Log.d(TAG, "translateText: going to translate $text")
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        // Download the model if necessary (suspend function)
        if (!loadedModel.getAndSet(true))
            downloadModelIfNeeded(conditions)

        // Translate the text (suspend function)
        val translatedText = languageTranslator.translate(text).await()

        Log.d(TAG, "translateText: Text Translated $translatedText")
        return translatedText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        // Inflate layout for each item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_label, parent, false)
        return LabelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        // Get label data for the current position
        val label = labels[position]
        // Update label text to sentence case
        val displayLabel = label.first[0].uppercaseChar() + label.first.substring(1)
        holder.textViewLabel.text = displayLabel

        if (translations[position] == "")
            incrementActiveCoroutinesCount()

        // Launch translation job and add it to the list
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val result = translateText(displayLabel)
                if (translations[position] == labels[position].first || translations[position] != "") {
                    Log.d(
                        TAG,
                        "This is already translated: Label" + labels[position].first + ", translation: " + translations[position]
                    )
                } else {
                    Log.d(TAG, "Initial Translation: $result")
                    translations[position] = result
                    withContext(Dispatchers.Main) {

                        // Load Translation Data
                        if (translations[position].isNotEmpty()) {
                            holder.translationNotVisible.visibility = View.GONE
                            holder.textViewTimestamp.visibility = View.VISIBLE
                            holder.textViewTimestamp.text = "Translation: ${translations[position]}"
                            holder.textViewLabel.visibility= View.VISIBLE
                            holder.labeNotVisible.visibility = View.GONE
                        }

//                        holder.translationNotVisible.text = "Translation: ${translations[position]}"
                    }
                    decrementActiveCoroutinesCount()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error translating text: ", e)
            }
        }



        // Load image using Glide
        val imagePath =
            "images/" + FirebaseAuth.getInstance().currentUser!!.email + "/" + label.first + ".jpg"
        val storageRef = FirebaseStorage.getInstance().getReference().child(imagePath)
        var localFile: File? = null
        try {
            localFile = File(context.cacheDir, "image_${label.first}.jpg")
            if (localFile.exists()) {
                Glide.with(context)
                    .load(localFile)
                    .into(holder.imageView)
                return
            }
            localFile = File.createTempFile("image_${label.first}", ".jpg", context.cacheDir)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Error creating temporary file: " + e.message)
        }

        Glide.with(context)
            .load(R.drawable.imgs)
            .into(holder.imageView)

        if (localFile != null) {
            // Download the image file to local storage
            val finalLocalFile: File = localFile
            storageRef.getFile(localFile)
                .addOnSuccessListener { taskSnapshot: FileDownloadTask.TaskSnapshot? ->
                    // Load image using Glide from the local file
                    Glide.with(context)
                        .load(finalLocalFile)
                        .into(holder.imageView)
                }.addOnFailureListener { e: Exception ->
                    // Handle any errors
                    Log.e(TAG, "Error downloading image from Firebase Storage: " + e.message)
                }
        }
    }

    override fun getItemCount(): Int {
        return labels.size
    }

    class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var labeNotVisible: TextView
        var translationNotVisible: TextView
        var textViewLabel: TextView
        var textViewTimestamp: TextView
        var imageView: ImageView


        init {
            textViewLabel = itemView.findViewById(R.id.textViewLabel)
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp)
            imageView = itemView.findViewById(R.id.item_img)
            translationNotVisible = itemView.findViewById(R.id.translationNotVisible)
            labeNotVisible = itemView.findViewById(R.id.labeNotVisible)
        }
    }

    private fun incrementActiveCoroutinesCount() {
        synchronized(this) {
            activeCoroutinesCount++
        }
    }

    private fun decrementActiveCoroutinesCount() {
        synchronized(this) {
            activeCoroutinesCount--
            if (activeCoroutinesCount == 0) {

            }
        }
    }


    companion object {
        private const val TAG = "LabelAdapter"
    }
}
