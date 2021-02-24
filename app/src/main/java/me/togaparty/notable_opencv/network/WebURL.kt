 package me.togaparty.notable_opencv.network

class WebURL {
    companion object {
        private const val baseURL = "https://49c8b2f408e9.ngrok.io/"
        const val url = baseURL + "predict/"

        const val header = "/predict"
    }
}