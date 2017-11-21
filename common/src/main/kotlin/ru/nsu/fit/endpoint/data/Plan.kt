@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION")

package ru.nsu.fit.endpoint.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Plan(
        @JsonProperty("id")
        var id: UUID? = null,

        @JsonProperty("name")
        var name: String? = null,

        @JsonProperty("details")
        var details: String? = null,

        @JsonProperty("fee")
        var fee: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val plan = other as Plan

        return if (id != null) id == plan.id else plan.id == null
    }

    override fun hashCode(): Int {
        return if (id != null) id!!.hashCode() else 0
    }

    fun clone() = Plan(id, name, details, fee)
}