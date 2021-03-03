package me.togaparty.notable_android.data

import android.content.Context
import android.net.Uri
import android.util.Log
import me.togaparty.notable_android.ui.adapter.PredictionsAdapter
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import java.io.*

class InspectPrediction(val symbol: String, val in_Glossary: Boolean) {
    companion object {
        fun replacePredictionList(textFiles: Map<String, Uri>?, position: Int, context: Context, predictionsAdapter: PredictionsAdapter, predictionsList: ArrayList<InspectPrediction>) {

            val textFileUri = textFiles?.get("predictions$position.txt")
            val textFile = readTextFile(textFileUri, context)
            predictionsList.clear()
            val list = textFile.split(',')
            for (text in list) {
                if (text.isNotEmpty()) {
                    val size = predictionsList.size-1
                    predictionsList.add(InspectPrediction(text, true))
                }
            }
            predictionsAdapter.notifyDataSetChanged()
        }

        fun createPredictionList(textFiles: Map<String, Uri>?, position: Int, context: Context): ArrayList<InspectPrediction> {
            if(textFiles != null) {
                Log.d(TAG, "Textfiles is not null")
                if(textFiles.isEmpty()) {
                    Log.d(TAG, "Textfiles is empty")
                }else{
                    Log.d(TAG, "Textfiles is not empty")
                }
            } else {
                Log.d(TAG, "Textfiles is null")
            }
            val predictionsList = ArrayList<InspectPrediction>()
            val textFileUri = textFiles?.get("predictions$position.txt")
            val textFile = readTextFile(textFileUri, context)
            val list = textFile.split(',')
            for (text in list) {
                if (text.isNotEmpty()) {
                    predictionsList.add(InspectPrediction(text, true))
                }
            }
            return predictionsList
        }

        private fun readTextFile(uri: Uri?, context: Context): String {

            var reader: BufferedReader? = null
            val builder = StringBuilder()

            uri?.let{
                fileUri ->
                val file = File(fileUri.path!!)
                if (file.exists()){
                    Log.d(TAG, "File exists")
                } else {
                    Log.d(TAG, "File doesn't exist")
                }
            }


            try {
                val n = uri?.let { context.contentResolver.openInputStream(it) }
                reader = BufferedReader(InputStreamReader(n))
                //reader = BufferedReader(InputStreamReader(uri?.let { context.contentResolver.openInputStream(it) }))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.appendLine(line)
                    builder.append(',')
                }


            } catch (e: IOException) {
                Log.d("Inspect", "Buffered Reader Error")
                Log.d("Inspect", e.printStackTrace().toString())
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        Log.d("Inspect", "Buffered Reader Close Error")
                        Log.d("Inspect", e.printStackTrace().toString())
                    }
                }
            }
            return builder.toString()
        }
    }
}