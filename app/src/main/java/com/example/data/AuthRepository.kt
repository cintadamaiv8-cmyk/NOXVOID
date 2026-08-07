package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthRepository(private val context: Context) {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val NAMA_KEY = stringPreferencesKey("nama")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val TAG_KEY = stringPreferencesKey("tag")
    
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[TOKEN_KEY] }
    val namaFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[NAMA_KEY] }
    val roleFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[ROLE_KEY] }
    val tagFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[TAG_KEY] }

    suspend fun saveAuthData(token: String, nama: String, role: String, tag: String = "") {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[NAMA_KEY] = nama
            prefs[ROLE_KEY] = role
            prefs[TAG_KEY] = tag
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
