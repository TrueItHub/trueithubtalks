package xyz.savvamirzoyan.trueithubtalks.repository.model.jsonconvertable

import kotlinx.serialization.Serializable

@Serializable
data class Wrapper<T>(val type: String, val data: T)
