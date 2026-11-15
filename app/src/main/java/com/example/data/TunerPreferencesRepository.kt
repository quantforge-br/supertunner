package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.InstrumentType
import com.example.domain.TuningCatalog
import com.example.domain.TuningPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tunerDataStore: DataStore<Preferences> by preferencesDataStore(name = "tuner_prefs")

data class SavedTunerPrefs(
    val instrument: InstrumentType,
    val tuningId: String
)

class TunerPreferencesRepository(private val context: Context) {

    private object Keys {
        val INSTRUMENT = stringPreferencesKey("selected_instrument")
        val TUNING_ID = stringPreferencesKey("selected_tuning_id")
    }

    val preferencesFlow: Flow<SavedTunerPrefs> = context.tunerDataStore.data.map { prefs ->
        SavedTunerPrefs(
            instrument = InstrumentType.fromId(prefs[Keys.INSTRUMENT]),
            tuningId = prefs[Keys.TUNING_ID] ?: TuningCatalog.presets.first().id
        )
    }

    suspend fun saveInstrument(instrument: InstrumentType) {
        context.tunerDataStore.edit { prefs ->
            prefs[Keys.INSTRUMENT] = instrument.id
        }
    }

    suspend fun saveTuning(preset: TuningPreset) {
        context.tunerDataStore.edit { prefs ->
            prefs[Keys.TUNING_ID] = preset.id
        }
    }
}
