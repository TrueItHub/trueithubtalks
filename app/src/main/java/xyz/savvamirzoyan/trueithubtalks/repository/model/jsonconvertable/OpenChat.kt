package xyz.savvamirzoyan.trueithubtalks.repository.model.jsonconvertable

import kotlinx.serialization.Serializable

@Serializable
data class OpenChat(val username: String, val token: String)
