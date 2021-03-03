package me.togaparty.notable_android.data

import android.content.Context
import android.net.Uri
import me.togaparty.notable_android.ui.adapter.PredictionsAdapter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class InspectPrediction(val symbol: String, val in_Glossary: Boolean) {
    companion object {
        fun replacePredictionList(textFiles: Map<String, Uri>?, position: Int, context: Context, predictionsAdapter: PredictionsAdapter, predictionsList: ArrayList<InspectPrediction>) {
            val textFileUri = textFiles?.get("predictions$position.txt")
            val textFile = readTextFile(textFileUri, context)
            predictionsList.clear()
            val list = textFile.split(',')
            for (text in list) {
                if (!text.isEmpty()) {
                    val size = predictionsList.size-1
                    predictionsList.add(InspectPrediction(text.toString(), true))
                }
            }
            predictionsAdapter.notifyDataSetChanged()
        }

        fun createPredictionList(textFiles: Map<String, Uri>?, position: Int, context: Context): ArrayList<InspectPrediction> {
            val predictionsList = ArrayList<InspectPrediction>()
            val textFileUri = textFiles?.get("predictions$position.txt")
            val textFile = readTextFile(textFileUri, context)
            val list = textFile.split(',')
            for (text in list) {
                if (!text.isEmpty()) {
                    predictionsList.add(InspectPrediction(text.toString(), true))
                }
            }
            return predictionsList
        }

        private fun readTextFile(uri: Uri?, context: Context): String {
            var reader: BufferedReader? = null
            val builder = StringBuilder()
            try {
                reader = BufferedReader(InputStreamReader(uri?.let { context.contentResolver.openInputStream(it) }))
                var line: String? = ""
                while (reader.readLine().also { line = it } != null) {
                    builder.appendLine(line)
                    builder.append(',')
                }
            } catch (e: IOException) {
                Log.d("Inspect", e.printStackTrace().toString())
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        Log.d("Inspect", e.printStackTrace().toString())
                    }
                }
            }
            return builder.toString()
        }
    }
}