package ru.nsu.fit.endpoint.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Customer (
    @JsonProperty("id")
    var id: UUID? = null,

    @JsonProperty("firstName")
    var firstName: String? = null,

    @JsonProperty("lastName")
    var lastName: String? = null,

    @JsonProperty("login")
    var login: String? = null,

    @JsonProperty("pass")
    var pass: String? = null,

    @JsonProperty("balance")
    var balance: Int = 0
) {
    fun equalsIgnoringFirstAndLastName(o: Customer?): Boolean {
        val other = o ?: return false
        if (this === other) return true
        return when {
            javaClass != other.javaClass -> false
            else -> other.id == id &&
                    other.login == login &&
                    other.balance == balance &&
                    other.pass == pass
        }
    }

    override fun hashCode(): Int {
        val finalId = id
        return finalId?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val customer = other as Customer

        return if (id != null) id == customer.id else customer.id == null
    }

    fun clone() = Customer(id, firstName, lastName, login, pass, balance)
}