package me.togaparty.notable_android.data

import android.net.Uri

class InspectPrediction (val symbol: String, val in_Glossary: Boolean) {
    companion object {
        fun createPredictionList(textFiles: Map<String, Uri>?) : ArrayList<InspectPrediction> {
            val predictionsList = ArrayList<InspectPrediction>()
            val sampleList: List<String> = listOf("clef-C3", "timeSignature-2/3", "rest-eighth","keySignature-GM","note-A#5_sixteenth","note-B5_quarter","note-F4_half","barline")
            //Artificial data, replace with ZIP extracted data files
            if (textFiles != null) {
                for(key in textFiles.keys){

                }
            }
            for (item in sampleList.indices) {
                predictionsList.add(InspectPrediction(sampleList[item], item > -1))
            }
            return predictionsList
        }
    }
}