package me.togaparty.notable_opencv.ui.items

class InspectPrediction (val symbol: String, val in_Glossary: Boolean) {
    companion object {
        private var lastEntry = 0
        fun createPredictionList(num_Symbols: Int) : ArrayList<InspectPrediction> {
            val predictionsList = ArrayList<InspectPrediction>()
            val sampleList: List<String> = listOf("clef-C3", "timeSignature-2/3", "rest-eighth","keySignature-GM","note-A#5_sixteenth","note-B5_quarter","note-F4_half","barline")
            //Artificial data, replace with ZIP extracted data files
            for (item in sampleList.indices) {
                predictionsList.add(InspectPrediction(sampleList[item], item > -1))
            }
            return predictionsList
        }
    }
}