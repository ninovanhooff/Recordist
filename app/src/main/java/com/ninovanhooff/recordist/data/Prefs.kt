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
 */
package com.ninovanhooff.recordist.data

import com.dimowner.phonograph.data.PhonographPrefs

interface Prefs : PhonographPrefs {
    val isFirstRun: Boolean
    fun firstRunExecuted()
    fun setStoreDirPublic(b: Boolean)
    var isAskToRenameAfterStopRecording: Boolean
    fun hasAskToRenameAfterStopRecordingSetting(): Boolean
    var activeRecord: Long
    override fun getRecordCounter(): Long
    override fun incrementRecordCounter()
    fun setAppThemeColor(colorMapPosition: Int)
    val themeColor: Int
    fun setRecordInStereo(stereo: Boolean)
    var isKeepScreenOn: Boolean
    fun setFormat(f: Int)
    override fun getFormat(): Int
    fun setBitrate(q: Int)
    override fun getBitrate(): Int
    fun setSampleRate(rate: Int)
    override fun getSampleRate(): Int
    fun setRecordOrder(order: Int)
    val recordsOrder: Int
    fun setNamingFormat(format: Int)
    override fun getNamingFormat(): Int
}