package me.togaparty.notable_android.data.files

import android.net.Uri
import me.togaparty.notable_android.ui.adapter.PredictionsAdapter
import java.io.File

class InspectPrediction(val symbol: String, val in_Glossary: Boolean) {
    companion object {
        fun replacePredictionList(
            textFiles: Map<String, Uri>?,
            position: Int,
            predictionsAdapter: PredictionsAdapter,
            predictionsList: ArrayList<InspectPrediction>
        ) {

            val textFileUri = textFiles?.get("predictions$position")
                ?: throw IllegalStateException("Null object is read")

            val list = readTextFile(textFileUri)
            predictionsList.clear()
            for (text in list) {
                if (text.isNotEmpty()) {
                    predictionsList.add(InspectPrediction(text, true))
                }
            }
            predictionsAdapter.notifyDataSetChanged()
        }

        fun createPredictionList(textFiles: Map<String, Uri>?, position: Int): ArrayList<InspectPrediction> {

            val predictionsList = arrayListOf<InspectPrediction>()
            val textFileUri = textFiles?.get("predictions$position")
                ?: throw IllegalStateException("Null object is read")

            val list = readTextFile(textFileUri)
            for (text in list) {
                if (text.isNotEmpty()) {
                    predictionsList.add(InspectPrediction(text, true))
                }
            }
            return predictionsList
        }

        private fun readTextFile(uri: Uri): List<String> =
            File(uri.path).useLines { it.toSortedSet().toList() }
    }
}