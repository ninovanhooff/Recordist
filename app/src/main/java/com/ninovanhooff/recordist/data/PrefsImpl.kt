/*
 * Copyright 2018 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with(the License.
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
import android.content.res.Resources
import androidx.annotation.StringRes
import com.ninovanhooff.phonograph.PhonographConstants
import com.ninovanhooff.phonograph.util.FileUtil
import com.ninovanhooff.recordist.R

/**
 * App preferences implementation
 */
class PrefsImpl private constructor(context: Context) : Prefs {
    private val resources: Resources
    private val sharedPreferences: SharedPreferences
    private val publicRecordingDirName: String
    override val isFirstRun: Boolean
        get() = !sharedPreferences.contains(PREF_KEY_IS_FIRST_RUN) || sharedPreferences.getBoolean(PREF_KEY_IS_FIRST_RUN, false)

    init {
        val prefsName = context.packageName + "_preferences"
        sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        resources = context.resources
        val appName = context.getString(R.string.app_name)
        publicRecordingDirName = FileUtil.removeUnallowedSignsFromName(appName)
    }

    override fun firstRunExecuted() {
        with(sharedPreferences.edit()) {
            putBoolean(PREF_KEY_IS_FIRST_RUN, false)
            putBoolean(PREF_KEY_IS_STORE_DIR_PUBLIC, true)
            apply()
        }
    }

    override fun isStoreDirPublic(): Boolean {
        return sharedPreferences.contains(PREF_KEY_IS_STORE_DIR_PUBLIC) && sharedPreferences.getBoolean(PREF_KEY_IS_STORE_DIR_PUBLIC, true)
    }

    override fun setStoreDirPublic(b: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(PREF_KEY_IS_STORE_DIR_PUBLIC, b)
            apply()
        }
    }

    override var isAskToRenameAfterStopRecording: Boolean
        get() = sharedPreferences.contains(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING) && sharedPreferences.getBoolean(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING, true)
        set(b) {
            with(sharedPreferences.edit()) {
                putBoolean(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING, b)
                apply()
            }
        }

    override fun hasAskToRenameAfterStopRecordingSetting(): Boolean {
        return sharedPreferences.contains(PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING)
    }

    override var activeRecord: Long
        get() = sharedPreferences.getLong(PREF_KEY_ACTIVE_RECORD, -1)
        set(id) {
            with(sharedPreferences.edit()) {
                putLong(PREF_KEY_ACTIVE_RECORD, id)
                apply()
            }
        }

    override fun getRecordCounter(): Long {
        return sharedPreferences.getLong(PREF_KEY_RECORD_COUNTER, 0)
    }

    override fun incrementRecordCounter() {
        with(sharedPreferences.edit()) {
            putLong(PREF_KEY_RECORD_COUNTER, recordCounter + 1)
            apply()
        }
            
    }

    override fun setAppThemeColor(colorMapPosition: Int) {
        with(sharedPreferences.edit()) {
            putInt(PREF_KEY_THEME_COLORMAP_POSITION, colorMapPosition)
            apply()
        }
    }

    override val themeColor: Int
        get() = sharedPreferences.getInt(PREF_KEY_THEME_COLORMAP_POSITION, 0)

    override fun setRecordInStereo(stereo: Boolean) {
        with(sharedPreferences.edit()) {
            putInt(PREF_KEY_RECORD_CHANNEL_COUNT, if (stereo) PhonographConstants.RECORD_AUDIO_STEREO else PhonographConstants.RECORD_AUDIO_MONO)
            apply()
        }
    }

    override fun getRecordChannelCount(): Int {
        return if(getBoolean(R.string.pref_key_channel_count, true)){
            PhonographConstants.RECORD_AUDIO_STEREO
        } else {
            PhonographConstants.RECORD_AUDIO_MONO
        }
    }

    override var isKeepScreenOn: Boolean
        get() = sharedPreferences.getBoolean(PREF_KEY_KEEP_SCREEN_ON, false)
        set(on) {
            with(sharedPreferences.edit()) {
                putBoolean(PREF_KEY_KEEP_SCREEN_ON, on)
                apply()
            }
        }

    override fun setFormat(f: Int) {
        putInt(R.string.pref_key_format, f)
    }

    override fun getFormat(): Int {
        return getInt(R.string.pref_key_format)
    }

    override fun setBitrate(q: Int) {
        putInt(R.string.pref_key_bitrate, q)
    }

    override fun getBitrate(): Int {
        return getInt(R.string.pref_key_bitrate)
    }

    override fun setSampleRate(rate: Int) {
        putInt(R.string.pref_key_sample_rate, rate)
    }

    override fun getSampleRate(): Int {
        return getInt(R.string.pref_key_sample_rate)
    }

    /** Not user-settable for now. Returns app name  */
    override fun getPublicRecordingDirName(): String {
        return publicRecordingDirName
    }

    override fun setRecordOrder(order: Int) {
        with(sharedPreferences.edit()) {
            putInt(PREF_KEY_RECORDS_ORDER, order)
            apply()
        }
    }

    override val recordsOrder: Int
        get() = sharedPreferences.getInt(PREF_KEY_RECORDS_ORDER, PhonographConstants.SORT_DATE)

    override fun setNamingFormat(format: Int) {
        putInt(R.string.pref_key_naming_format, format)
    }

    override fun getNamingFormat(): Int {
        return getInt(R.string.pref_key_naming_format)
    }

    fun getString(@StringRes keyResourceId: Int, default: String? = null): String? {
        return sharedPreferences.getString(key(keyResourceId), default)
    }

    fun putString(@StringRes keyResourceId: Int, value: String){
        with(sharedPreferences.edit()){
            putString(key(keyResourceId), value)
            apply()
        }
    }

    fun getInt(@StringRes keyResourceId: Int): Int {
        val default = defaultKey(keyResourceId, "integer").let {
            if (it == 0){
                0
            } else {
                try {
                    resources.getInteger(it)
                } catch (e: Resources.NotFoundException) {
                    0
                }
            }
        }

        return getInt(keyResourceId, default)
    }

    fun getInt(@StringRes keyResourceId: Int, default: Int = 0): Int {
        return getString(keyResourceId)?.let { Integer.parseInt(it) } ?: default
    }

    fun putInt(@StringRes keyResourceId: Int, value: Int){
        putString(keyResourceId, value.toString())
    }

    fun getBoolean(@StringRes keyResourceId: Int): Boolean {
        val default = defaultKey(keyResourceId, "bool").let {
            if (it == 0){
                false
            } else {
                try {
                    resources.getBoolean(it)
                } catch (e: Resources.NotFoundException) {
                    false
                }
            }
        }

        return sharedPreferences.getBoolean(key(keyResourceId), default)
    }

    fun getBoolean(@StringRes keyResourceId: Int, default: Boolean): Boolean =
            sharedPreferences.getBoolean(key(keyResourceId), default)

    fun putBoolean(@StringRes keyResourceId: Int, value: Boolean){
        with(sharedPreferences.edit()){
            putBoolean(key(keyResourceId), value)
            apply()
        }

    }

    private fun key(keyResourceId: Int) = resources.getString(keyResourceId)

    /** @return 0 if not found, resource id otherwise */
    private fun defaultKey(keyResourceId: Int, type: String): Int {
        val defaultKeyName = resources.getResourceEntryName(keyResourceId).replace("pref_key_", "pref_default_")
        return resources.getIdentifier(defaultKeyName, type, resources.getResourcePackageName(keyResourceId))
    }

    companion object {
        private const val PREF_KEY_IS_FIRST_RUN = "is_first_run"
        private const val PREF_KEY_IS_STORE_DIR_PUBLIC = "is_store_dir_public"
        private const val PREF_KEY_IS_ASK_TO_RENAME_AFTER_STOP_RECORDING = "is_ask_rename_after_stop_recording"
        private const val PREF_KEY_ACTIVE_RECORD = "active_record"
        private const val PREF_KEY_RECORD_COUNTER = "record_counter"
        private const val PREF_KEY_THEME_COLORMAP_POSITION = "theme_color"
        private const val PREF_KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val PREF_KEY_RECORD_CHANNEL_COUNT = "pref_channel_count"
        private const val PREF_KEY_RECORDS_ORDER = "pref_records_order"
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
}