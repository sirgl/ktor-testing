package ru.nsu.fit.endpoint.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class Subscription(
        @JsonProperty("id")
        var id: UUID? = null,

        @JsonProperty("customerId")
        var customerId: UUID? = null,

        @JsonProperty("planId")
        var planId: UUID? = null
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || javaClass != other.javaClass) return false

                val that = other as Subscription

                return if (id != null) id == that.id else that.id == null
        }

        override fun hashCode(): Int {
                return if (id != null) id!!.hashCode() else 0
        }

        fun clone() = Subscription(id, customerId, planId)
}