package me.togaparty.notable_opencv.model

class Inspect_Prediction (val symbol: String, val in_Glossary: Boolean) {
    companion object {
        private var lastEntry = 0
        fun createPredictionList(num_Symbols: Int) : ArrayList<Inspect_Prediction> {
            val predictionsList = ArrayList<Inspect_Prediction>()
            val sampleList: List<String> = listOf("clef-C3", "timeSignature-2/3", "rest-eighth","keySignature-GM","note-A#5_sixteenth","note-B5_quarter","note-F4_half","barline")
            //Artificial data, replace with ZIP extracted data files
            for (i in 0 until sampleList.size-1) {
                predictionsList.add(Inspect_Prediction(sampleList[i], i > -1))
            }
            return predictionsList
        }
    }
}