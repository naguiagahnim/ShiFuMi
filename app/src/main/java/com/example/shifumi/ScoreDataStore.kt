package com.example.shifumi

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "score_prefs")

object ScorePreferences {
    val HIGH_SCORE = intPreferencesKey("high_score")
}

suspend fun Context.updateHighScore(newScore: Int) {
    dataStore.edit { preferences ->
        val currentHighScore = preferences[ScorePreferences.HIGH_SCORE] ?: 0
        if (newScore > currentHighScore) {
            preferences[ScorePreferences.HIGH_SCORE] = newScore
        }
    }
}

val Context.highScoreFlow: Flow<Int>
    get() = dataStore.data.map { preferences ->
        preferences[ScorePreferences.HIGH_SCORE] ?: 0
    }
