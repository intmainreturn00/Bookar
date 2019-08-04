package com.intmainreturn00.arbooks.data.db

import androidx.room.TypeConverter
import com.intmainreturn00.arbooks.domain.Cover
import com.intmainreturn00.arbooks.domain.ImageCover
import com.intmainreturn00.arbooks.domain.Shelf
import com.intmainreturn00.arbooks.domain.TemplateCover
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromCoverToString(cover: Cover): String {
        val json = JSONObject()

        json.put("width", cover.width)
        json.put("height", cover.height)
        json.put("spineColor", cover.spineColor)
        json.put("type", Cover.toInt(cover))

        val notUsed = when (cover) {
            is ImageCover -> {
                json.put("url", cover.url)
            }
            is TemplateCover -> {
                json.put("textColor", cover.textColor)
            }
        }

        return json.toString()
    }

    @TypeConverter
    fun fromStringToCover(jsonString: String): Cover {
        val json = JSONObject(jsonString)
        val type = json.getInt("type")
        val width = json.getInt("width")
        val height= json.getInt("height")
        val spineColor= json.getInt("spineColor")

        when (type) {
            0 -> {
                return ImageCover(
                    width = width,
                    height = height,
                    spineColor = spineColor,
                    url = json.getString("url")
                )
            }
            else -> {
                return TemplateCover(
                    width = width,
                    height = height,
                    spineColor = spineColor,
                    textColor = json.getInt("textColor")
                )

            }
        }
    }

    @TypeConverter
    fun fromShelvesToString(shelves: List<Shelf>): String {
        val json = JSONArray()
        shelves.forEach {
            val jsonObject = JSONObject()
            jsonObject.put("id", it.id)
            jsonObject.put("name", it.name)
            json.put(jsonObject)
        }
        return json.toString()
    }

    @TypeConverter
    fun fromStringToShelves(jsonString: String) : List<Shelf> {
        val res = mutableListOf<Shelf>()
        val json = JSONArray(jsonString)
        for(i in 0 until json.length()) {
            val jsonObject = json.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val name = jsonObject.getString("name")
            res.add(Shelf(id, name))
        }
        return res
    }
}