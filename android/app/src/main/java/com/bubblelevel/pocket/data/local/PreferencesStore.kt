package com.bubblelevel.pocket.data.local

import android.content.Context
import com.bubblelevel.pocket.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class PreferencesStore(context: Context) {
    private val preferences = context.getSharedPreferences("bubble_level_pocket", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val _history = MutableStateFlow(HistoryState())
    val history = _history.asStateFlow()
    private val _settings = MutableStateFlow(AppSettings())
    val settings = _settings.asStateFlow()
    private val _calibrations = MutableStateFlow(CalibrationState())
    val calibrations = _calibrations.asStateFlow()
    private var loaded = false
    private val committedPayloads = mutableMapOf<String, String?>()
    private val failedRecoveryKeys = mutableSetOf<String>()

    suspend fun load(force: Boolean = false) = mutex.withLock {
        if (!loaded || force) loadLocked()
    }

    private suspend fun loadLocked() = withContext(Dispatchers.IO) {
        fun payload(key: String): String? {
            if (committedPayloads.containsKey(key)) return committedPayloads[key]
            val value = runCatching { preferences.getString(key, null) }.getOrElse { preferences.all[key].toString() }
            committedPayloads[key] = value
            return value
        }
        val historyRaw = payload("history")
        val settingsRaw = payload("settings")
        val calibrationRaw = payload("calibrations")
        val parsed = PreferenceJsonCodec.readHistory(historyRaw)
        if (parsed.corrupt && historyRaw != null) quarantine("history", historyRaw)
        val calibration = PreferenceJsonCodec.readCalibrations(calibrationRaw)
        if (calibration.invalidSlots.isNotEmpty() && calibrationRaw != null) quarantine("calibrations", calibrationRaw)
        if (settingsRaw != null && runCatching {
            val value = org.json.JSONObject(settingsRaw)
            require(value.getInt("schema") == 1)
            AngleUnit.valueOf(value.getString("unit"))
            value.getBoolean("sound")
            value.getBoolean("haptics")
        }.isFailure) quarantine("settings", settingsRaw)
        _history.update { parsed.copy(readOnly = parsed.corrupt) }
        _settings.update { PreferenceJsonCodec.readSettings(settingsRaw) }
        _calibrations.update { calibration }
        loaded = true
    }

    private fun quarantine(key: String, payload: String): Boolean = runCatching {
        if (preferences.contains("recovery_$key") && key !in failedRecoveryKeys) true
        else preferences.edit().putString("recovery_$key", payload).commit().also { if (it) failedRecoveryKeys.remove(key) else failedRecoveryKeys.add(key) }
    }.getOrDefault(false)

    private suspend fun commit(key: String, payload: String): Boolean {
        var acknowledged = false
        val completed = withTimeoutOrNull(2000L) {
            withContext(Dispatchers.IO) {
                runCatching { preferences.edit().putInt("schema", 1).putString(key, payload).commit() }.getOrDefault(false).also { acknowledged = it }
            }
        }
        val success = completed ?: acknowledged
        if (success) committedPayloads[key] = payload
        return success
    }

    suspend fun save(measurement: Measurement): WriteOutcome = mutex.withLock {
        if (!loaded) loadLocked()
        val current = _history.value
        if (current.entries.any { it.id == measurement.id }) return@withLock WriteOutcome.SUCCESS
        if (current.readOnly) return@withLock WriteOutcome.READ_ONLY
        if (current.entries.size >= 100) return@withLock WriteOutcome.CAPACITY
        val next = (current.entries + measurement).sortedWith(compareByDescending<Measurement> { it.capturedAt }.thenBy { it.id })
        if (commit("history", PreferenceJsonCodec.history(next))) {
            _history.update { it.copy(entries = next) }
            WriteOutcome.SUCCESS
        } else WriteOutcome.FAILED
    }

    suspend fun delete(ids: Set<String>): Boolean = mutex.withLock {
        if (!loaded) loadLocked()
        if (_history.value.readOnly) return@withLock false
        val next = _history.value.entries.filterNot { it.id in ids }
        if (commit("history", PreferenceJsonCodec.history(next))) {
            _history.update { it.copy(entries = next) }
            true
        } else false
    }

    suspend fun recover(): Boolean = mutex.withLock {
        if (!loaded) loadLocked()
        val backedUp = withContext(Dispatchers.IO) {
            runCatching { committedPayloads["history"]?.let { quarantine("history", it) } ?: true }.getOrDefault(false)
        }
        if (!backedUp) return@withLock false
        if (commit("history", PreferenceJsonCodec.history(_history.value.entries))) {
            _history.update { it.copy(corrupt = false, readOnly = false) }
            true
        } else false
    }

    suspend fun setSettings(value: AppSettings): Boolean = mutex.withLock {
        if (!loaded) loadLocked()
        if (commit("settings", PreferenceJsonCodec.settings(value))) {
            _settings.update { value }
            true
        } else false
    }

    suspend fun setCalibration(slot: CalibrationSlot, value: Calibration?): Boolean = mutex.withLock {
        if (!loaded) loadLocked()
        val next = _calibrations.value.entries.toMutableMap().apply { if (value == null) remove(slot) else put(slot, value) }.toMap()
        if (commit("calibrations", PreferenceJsonCodec.calibrations(next))) {
            _calibrations.update { it.copy(entries = next, invalidSlots = it.invalidSlots - slot) }
            true
        } else false
    }
}
