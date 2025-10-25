package com.uvg.mypokedex.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class OrderType { NUMBER_ASC, NUMBER_DESC, NAME_ASC, NAME_DESC }

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPrefsDataStore(private val context: Context) {
    private object Keys {
        val ORDER = stringPreferencesKey("order_type")
    }

    val orderFlow: Flow<OrderType> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.ORDER]) {
            OrderType.NUMBER_ASC.name -> OrderType.NUMBER_ASC
            OrderType.NUMBER_DESC.name -> OrderType.NUMBER_DESC
            OrderType.NAME_ASC.name -> OrderType.NAME_ASC
            OrderType.NAME_DESC.name -> OrderType.NAME_DESC
            else -> OrderType.NUMBER_ASC
        }
    }

    suspend fun setOrder(order: OrderType) {
        context.dataStore.edit { it[Keys.ORDER] = order.name}
        }
}