package com.mfa.utils

import android.content.Context
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.mfa.R
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

        fun isCameraLiveViewportEnabled(context: Context): Boolean {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val prefKey = context.getString(R.string.pref_key_camera_live_viewport)
            return sharedPreferences.getBoolean(prefKey, true)
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
            var embeddingFloatArr: FloatArray = emptyArray<Float>().toFloatArray()
            if(!faceEmbedString!!.isEmpty()){
                 embeddingFloatArr = Gson().fromJson(faceEmbedString, type)
            }
            return embeddingFloatArr
        }

        fun clearData(context: Context){
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().clear().apply()
        }

        /**
         * Mode type preference is backed by [android.preference.ListPreference] which only support
         * storing its entry value as string type, so we need to retrieve as string and then convert to
         * integer.
         */
        private fun getModeTypePreferenceValue(
            context: Context, @StringRes prefKeyResId: Int, defaultValue: Int
        ): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val prefKey = context.getString(prefKeyResId)
            return sharedPreferences.getString(prefKey, defaultValue.toString())!!.toInt()
        }

        fun getFaceDetectorOptions(context: Context): FaceDetectorOptions {
            val landmarkMode: Int = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_landmark_mode,
                FaceDetectorOptions.LANDMARK_MODE_NONE
            )
            val contourMode: Int = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_contour_mode,
                FaceDetectorOptions.CONTOUR_MODE_NONE
            )
            val classificationMode: Int = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_classification_mode,
                FaceDetectorOptions.CLASSIFICATION_MODE_NONE
            )
            val performanceMode: Int = getModeTypePreferenceValue(
                context,
                R.string.pref_key_live_preview_face_detection_performance_mode,
                FaceDetectorOptions.PERFORMANCE_MODE_FAST
            )

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val enableFaceTracking = sharedPreferences.getBoolean(
                context.getString(R.string.pref_key_live_preview_face_detection_face_tracking), false
            )
            val minFaceSize = sharedPreferences.getString(
                context.getString(R.string.pref_key_live_preview_face_detection_min_face_size),
                "0.5"
            )!!.toFloat()

            val optionsBuilder = FaceDetectorOptions.Builder()
                .setLandmarkMode(landmarkMode)
                .setContourMode(contourMode)
                .setClassificationMode(classificationMode)
                .setPerformanceMode(performanceMode)
                .setMinFaceSize(minFaceSize)
            if (enableFaceTracking) {
                optionsBuilder.enableTracking()
            }
            return optionsBuilder.build()
        }

        fun shouldHideDetectionInfo(context: Context): Boolean {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val prefKey = context.getString(R.string.pref_key_info_hide)
            return sharedPreferences.getBoolean(prefKey, false)
        }
    }
}