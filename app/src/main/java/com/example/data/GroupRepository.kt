package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first

val Context.groupDataStore by preferencesDataStore(name = "group_prefs")

class GroupRepository(private val context: Context) {
    private val NAME_KEY = stringPreferencesKey("group_name")
    private val DESC_KEY = stringPreferencesKey("group_desc")
    private val BANNER_KEY = stringPreferencesKey("group_banner")
    private val AVATAR_KEY = stringPreferencesKey("group_avatar")

    val nameFlow: Flow<String> = context.groupDataStore.data.catch { emit(emptyPreferences()) }.map { it[NAME_KEY] ?: "Clash Of Clans Community" }
    val descFlow: Flow<String> = context.groupDataStore.data.catch { emit(emptyPreferences()) }.map { it[DESC_KEY] ?: "Selamat datang di Clash Of Clans Community.\n\nTempat berdiskusi strategi, war, dan rekrutmen klan secara private dan eksklusif. Patuhi aturan dan jaga kesopanan sesama anggota NOXVOID." }
    val bannerFlow: Flow<String> = context.groupDataStore.data.catch { emit(emptyPreferences()) }.map { it[BANNER_KEY] ?: "" }
    val avatarFlow: Flow<String> = context.groupDataStore.data.catch { emit(emptyPreferences()) }.map { it[AVATAR_KEY] ?: "" }

    suspend fun saveGroupData(name: String, desc: String, banner: String, avatar: String) {
        context.groupDataStore.edit { prefs ->
            prefs[NAME_KEY] = name
            prefs[DESC_KEY] = desc
            prefs[BANNER_KEY] = banner
            prefs[AVATAR_KEY] = avatar
        }
    }
    
    suspend fun updateName(name: String) {
        context.groupDataStore.edit { it[NAME_KEY] = name }
    }
    suspend fun updateDesc(desc: String) {
        context.groupDataStore.edit { it[DESC_KEY] = desc }
    }
    suspend fun updateBanner(banner: String) {
        context.groupDataStore.edit { it[BANNER_KEY] = banner }
    }
    suspend fun updateAvatar(avatar: String) {
        context.groupDataStore.edit { it[AVATAR_KEY] = avatar }
    }
    
    suspend fun getGroupName(): String = nameFlow.first()
    suspend fun getGroupDesc(): String = descFlow.first()
    suspend fun getGroupBanner(): String = bannerFlow.first()
    suspend fun getGroupAvatar(): String = avatarFlow.first()
}
