/*
 * Copyright 2018 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2020 Nino van Hooff
 * Lincensed under GPL
 */
package com.ninovanhooff.recordist.data

import android.content.Context
import android.content.SharedPreferences
import com.ninovanhooff.phonograph.PhonographConstants
import com.ninovanhooff.phonograph.util.FileUtil
import com.ninovanhooff.recordist.R

/**
 * App preferences implementation
 */
class PrefsImpl private constructor(context: Context) : Prefs {
    private val sharedPreferences: SharedPreferences
    private val publicRecordingDirName: String
    override val isFirstRun: Boolean
        get() = !sharedPreferences.contains(PREF_KEY_IS_FIRST_RUN) || sharedPreferences.getBoolean(PREF_KEY_IS_FIRST_RUN, false)

    override fun firstRunExecuted() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_KEY_IS_FIRST_RUN, false)
        editor.putBoolean(PREF_KEY_IS_STORE_DIR_PUBLIC, true)
        editor.apply()
    }

    override fun isStoreDirPublic(): Boolean {
        return sharedPreferences.contains(PREF_KEY_IS_STORE_DIR_PUBLIC) && sharedPreferences.getBoolean(PREF_KEY_IS_STORE_DIR_PUBLIC, true)
    }

    override fun setStoreDirPublic(b: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(PREF_KEY_IS_STORE_DIR_PUBLIC, b)
        editor.apply()
    }

    override var isAskToRenameAfterStopRecording: Boolean
        get() = sharedPreferences.contains(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING) && sharedPreferences.getBoolean(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING, true)
        set(b) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING, b)
            editor.apply()
        }

    override fun hasAskToRenameAfterStopRecordingSetting(): Boolean {
        return sharedPreferences.contains(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING)
    }

    override var activeRecord: Long
        get() = sharedPreferences.getLong(PREF_KEY_ACTIVE_RECORD, -1)
        set(id) {
            val editor = sharedPreferences.edit()
            editor.putLong(PREF_KEY_ACTIVE_RECORD, id)
            editor.apply()
        }

    override fun getRecordCounter(): Long {
        return sharedPreferences.getLong(PREF_KEY_RECORD_COUNTER, 0)
    }

    override fun incrementRecordCounter() {
        val editor = sharedPreferences.edit()
        editor.putLong(PREF_KEY_RECORD_COUNTER, recordCounter + 1)
        editor.apply()
    }

    override fun setAppThemeColor(colorMapPosition: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_THEME_COLORMAP_POSITION, colorMapPosition)
        editor.apply()
    }

    override val themeColor: Int
        get() = sharedPreferences.getInt(PREF_KEY_THEME_COLORMAP_POSITION, 0)

    override fun setRecordInStereo(stereo: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_RECORD_CHANNEL_COUNT, if (stereo) PhonographConstants.RECORD_AUDIO_STEREO else PhonographConstants.RECORD_AUDIO_MONO)
        editor.apply()
    }

    override fun getRecordChannelCount(): Int {
        return sharedPreferences.getInt(PREF_KEY_RECORD_CHANNEL_COUNT, PhonographConstants.RECORD_AUDIO_STEREO)
    }

    override var isKeepScreenOn: Boolean
        get() = sharedPreferences.getBoolean(PREF_KEY_KEEP_SCREEN_ON, false)
        set(on) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREF_KEY_KEEP_SCREEN_ON, on)
            editor.apply()
        }

    override fun setFormat(f: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_FORMAT, f)
        editor.apply()
    }

    override fun getFormat(): Int {
        return sharedPreferences.getInt(PREF_KEY_FORMAT, PhonographConstants.RECORDING_FORMAT_WAV)
    }

    override fun setBitrate(q: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_BITRATE, q)
        editor.apply()
    }

    override fun getBitrate(): Int {
        return sharedPreferences.getInt(PREF_KEY_BITRATE, PhonographConstants.RECORD_ENCODING_BITRATE_128000)
    }

    override fun setSampleRate(rate: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_SAMPLE_RATE, rate)
        editor.apply()
    }

    override fun getSampleRate(): Int {
        return sharedPreferences.getInt(PREF_KEY_SAMPLE_RATE, PhonographConstants.RECORD_SAMPLE_RATE_44100)
    }

    /** Not user-settable for now. Returns app name  */
    override fun getPublicRecordingDirName(): String {
        return publicRecordingDirName
    }

    override fun setRecordOrder(order: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_RECORDS_ORDER, order)
        editor.apply()
    }

    override val recordsOrder: Int
        get() = sharedPreferences.getInt(PREF_KEY_RECORDS_ORDER, PhonographConstants.SORT_DATE)

    override fun setNamingFormat(format: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PREF_KEY_NAMING_FORMAT, format)
        editor.apply()
    }

    override fun getNamingFormat(): Int {
        return sharedPreferences.getInt(PREF_KEY_NAMING_FORMAT, PhonographConstants.NAMING_COUNTED)
    }

    companion object {
        private const val PREF_NAME = "com.dimowner.audiorecorder.data.PrefsImpl"
        private const val PREF_KEY_IS_FIRST_RUN = "is_first_run"
        private const val PREF_KEY_IS_STORE_DIR_PUBLIC = "is_store_dir_public"
        private const val PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING = "is_ask_rename_after_stop_recording"
        private const val PREF_KEY_ACTIVE_RECORD = "active_record"
        private const val PREF_KEY_RECORD_COUNTER = "record_counter"
        private const val PREF_KEY_THEME_COLORMAP_POSITION = "theme_color"
        private const val PREF_KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val PREF_KEY_FORMAT = "pref_format"
        private const val PREF_KEY_BITRATE = "pref_bitrate"
        private const val PREF_KEY_SAMPLE_RATE = "pref_sample_rate"
        private const val PREF_KEY_RECORDS_ORDER = "pref_records_order"
        private const val PREF_KEY_NAMING_FORMAT = "pref_naming_format"
        //Recording prefs.
        private const val PREF_KEY_RECORD_CHANNEL_COUNT = "record_channel_count"
        @Volatile
        private var instance: PrefsImpl? = null

        fun getInstance(context: Context): PrefsImpl? {
            if (instance == null) {
                synchronized(PrefsImpl::class.java) {
                    if (instance == null) {
                        instance = PrefsImpl(context)
                    }
                }
            }
            return instance
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val appName = context.getString(R.string.app_name)
        publicRecordingDirName = FileUtil.removeUnallowedSignsFromName(appName)
    }
}