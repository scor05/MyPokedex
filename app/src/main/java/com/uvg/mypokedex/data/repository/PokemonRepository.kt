package com.uvg.mypokedex.data.repository

import android.content.Context
import androidx.room.Room
import com.uvg.mypokedex.data.connectivity.ConnectivityObserver
import com.uvg.mypokedex.data.local.db.AppDatabase
import com.uvg.mypokedex.data.local.db.CachedPokemon
import com.uvg.mypokedex.data.prefs.OrderType
import com.uvg.mypokedex.data.prefs.UserPrefsDataStore
import com.uvg.mypokedex.data.remote.RemoteDataSource
import com.uvg.mypokedex.data.remote.dto.PokemonDetailResponse
import com.uvg.mypokedex.data.remote.dto.PokemonListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokemonRepository(
    private val db: AppDatabase,
    private val prefs: UserPrefsDataStore,
    private val net: ConnectivityObserver
) {
    private val api = RemoteDataSource.api
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    val order: Flow<OrderType> = prefs.orderFlow

    val pokedex: Flow<List<CachedPokemon>> =
        prefs.orderFlow.flatMapLatest { ord ->
            when (ord) {
                OrderType.NUMBER_ASC -> db.pokemonDao().listByNumberAsc()
                OrderType.NUMBER_DESC -> db.pokemonDao().listByNumberDesc()
                OrderType.NAME_ASC -> db.pokemonDao().listByNameAsc()
                OrderType.NAME_DESC -> db.pokemonDao().listByNameDesc()
            }
        }.combine(net.isOnline) { local, online ->
            if (online) {
                scope.launch { refreshFirstPageIfEmpty() }
            }
            local
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    suspend fun setOrder(orderType: OrderType) {
        prefs.setOrder(orderType)
    }

    suspend fun refreshPage(limit: Int, offset: Int) {
        val list = api.getPokemonList(limit, offset)
        val items: List<CachedPokemon> = list.results
            .map { it.name }
            .mapNotNull { name -> runCatching { api.getPokemonDetail(name) }.getOrNull() }
            .map { d -> mapDetailToCached(d) }
        db.pokemonDao().upsertAll(items)
    }
    private suspend fun refreshFirstPageIfEmpty() {
        val current = db.pokemonDao().listByNumberAsc().first()
        if (current.isEmpty()) {
            refreshPage(limit = 20, offset = 0)
        }
    }

    private fun mapDetailToCached(d: PokemonDetailResponse): CachedPokemon {
        val id: Int = runCatching { d.id }.getOrDefault(0)
        val name: String = runCatching { d.name }.getOrDefault("")

        // Usamos solo el sprite seguro que existe en tu DTO
        val image: String = runCatching { d.sprites.front_default }.getOrNull() ?: ""

        val types: List<String> = runCatching {
            d.types.map { t -> t.type.name }
        }.getOrDefault(emptyList())

        val stats: Map<String, Int> = runCatching {
            d.stats.associate { s -> s.stat.name to s.base_stat }
        }.getOrDefault(emptyMap())

        return CachedPokemon(
            id = id,
            name = name,
            imageUrl = image,
            types = types,
            stats = stats,
            lastFetchedAt = System.currentTimeMillis()
        )
    }
    suspend fun getPokemonList(limit: Int, offset: Int): Result<PokemonListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPokemonList(limit, offset)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPokemonDetail(name: String): Result<PokemonDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPokemonDetail(name)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        fun create(context: Context): PokemonRepository {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "pokedex.db").build()
            val prefs = UserPrefsDataStore(context)
            val net = ConnectivityObserver(context)
            return PokemonRepository(db, prefs, net)
            }
        }
}