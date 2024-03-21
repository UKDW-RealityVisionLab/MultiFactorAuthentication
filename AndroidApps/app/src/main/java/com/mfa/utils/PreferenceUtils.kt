package com.mfa.utils

import android.content.Context
import androidx.annotation.Nullable
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class PreferenceUtils {

    private fun PreferenceUtils() {
    }

    companion object {
        private val POSE_DETECTOR_PERFORMANCE_MODE_FAST = 1
        private val STRING_USER_NAME = "STRING_USER_NAME"
        private val STRING_FACE_EMBEDING = "STRING_FACE_EMBEDING"

        fun saveString(context: Context, prefKeyId: String, @Nullable value: String?) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(prefKeyId, value)
                .apply()
        }

        fun saveUsername(context: Context, username: String?) {
            saveString(context, STRING_USER_NAME, username)
        }

        fun getUsername(context: Context): String? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(STRING_USER_NAME, "")
        }

        fun saveFaceEmbeddings(context: Context, embedingFloatList: List<String?>?) {
            val gson = Gson()
            val embedingString: String = gson.toJson(embedingFloatList)
            saveString(context, STRING_FACE_EMBEDING, embedingString)
        }

        fun getFaceEmbeddings(context: Context): FloatArray {
            val type: Type = object : TypeToken<FloatArray?>() {
            }.getType()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val faceEmbedString = sharedPreferences.getString(STRING_FACE_EMBEDING, "")
            val gson = Gson()
            val embeddingFloatArr: FloatArray = gson.fromJson(faceEmbedString, type)
            return embeddingFloatArr
        }

        fun clearData(context: Context){
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().clear()
            sharedPreferences.edit().apply()
        }
    }
}