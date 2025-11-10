
package com.uvg.mypokedex.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<TypeSlot>,
    val stats: List<StatSlot>
)

data class Sprites(
    val front_default: String?,
    val other: OtherSprites
)

data class OtherSprites(
    @SerializedName("official-artwork") val official_artwork: OfficialArtwork
)

data class OfficialArtwork(
    val front_default: String?
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String
)

data class StatSlot(
    val base_stat: Int,
    val stat: StatInfo
)

data class StatInfo(
    val name: String
)
