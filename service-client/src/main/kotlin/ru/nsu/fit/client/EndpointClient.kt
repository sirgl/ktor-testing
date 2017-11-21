package ru.nsu.fit.client

import ru.nsu.fit.endpoint.data.Customer
import java.util.*

interface EndpointClient {
    fun getHealthCheck() : Boolean
    fun getRole() : String?
    fun getCustomers() : List<Customer>
    fun createCustomer(customer: Customer) : Customer
    fun getCustomerId(customerLogin: String) : UUID?
    fun removeCustomer(id: UUID)
}

class UnauthorizedException : Exception()

class InternalError(override val message: String) : Exception()