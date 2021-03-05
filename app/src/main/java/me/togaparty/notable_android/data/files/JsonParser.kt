package me.togaparty.notable_android.data.files

import android.content.Context
import me.togaparty.notable_android.ui.items.CategoryItem
import me.togaparty.notable_android.utils.SingletonHolder
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset


private const val NAME = "name"
private const val DEFINITION = "definition"
class JsonParser(appContext: Context) {
	private val context = appContext

	private val json: JSONObject by lazy {
		JSONObject(
			try {
				val inputStream = context.assets?.open("glossary.json")
				val size = inputStream?.available()
				val buffer = size?.let { ByteArray(it) }
				val charset: Charset = Charsets.UTF_8
				inputStream?.read(buffer)
				inputStream?.close()
				buffer?.let { String(it, charset) }?: ""
			} catch (ex: IOException) {
				ex.printStackTrace()
				""
			}
		)
	}
	private val wikijson: JSONObject by lazy {
		JSONObject(
			try {
				val inputStream = context.assets?.open("test.json")
				val size = inputStream?.available()
				val buffer = size?.let { ByteArray(it) }
				val charset: Charset = Charsets.UTF_8
				inputStream?.read(buffer)
				inputStream?.close()
				buffer?.let { String(it, charset) }?: ""
			} catch (ex: IOException) {
				ex.printStackTrace()
				""
			}
		)
	}

	fun getNoteAndDefinition(key: String) : Pair<String, String> {
		val jsonObject = wikijson.getJSONObject(key)
		return Pair (
			jsonObject.getString(NAME),
			jsonObject.getString(DEFINITION)
		)
	}


	fun getList(key: String, itemID: Int) : MutableList<CategoryItem> {
		val list = mutableListOf<CategoryItem>()
		val userArray = json.getJSONArray(key)
		for (i in 0 until userArray.length()) {
			val termAndDefinition = userArray.getJSONObject(i)
			list.add(
				CategoryItem(
					itemID,
					termAndDefinition.getString(NAME),
					termAndDefinition.getString(DEFINITION)
				)
			)
		}
		return list
	}
	companion object : SingletonHolder<JsonParser, Context>(::JsonParser)
}









