package ru.nsu.fit.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import org.slf4j.LoggerFactory
import ru.nsu.fit.endpoint.data.Customer
import java.util.*

class RestClient(address: String,
                 private val login: String,
                 private val password: String) : EndpointClient {


    private val logger = LoggerFactory.getLogger("RestClient")
    private val mapper = jacksonObjectMapper()

    private val basePath = "$address/endpoint/rest"

    private inline fun <reified T> String.parse(): T = mapper.readValue(this, T::class.java)
    private inline fun <reified T> String.parseList(): List<T> =
            mapper.readValue(this, mapper.typeFactory.constructCollectionType(List::class.java, T::class.java))

    private fun <T : Any?> HttpResponse<T>.verifyStatus() : HttpResponse<T> {
        if(status == 401) throw UnauthorizedException()
        if(status != 200) throw InternalError("Wrong status: $status\n${this.body}")
        return this
    }

    private fun <T : Any?> HttpResponse<T>.log() : HttpResponse<T> {
        logger.debug("Request: $body with status $statusText")
        return this
    }

    override fun removeCustomer(id: UUID) {
        Unirest.delete("$basePath/remove_customer/$id")
                .header("Accept", "*/*")
                .basicAuth(login, password).asString()
                .log()
                .verifyStatus()
    }

    override fun getHealthCheck(): Boolean {
        val result = Unirest.get("$basePath/health_check")
                .basicAuth(login, password).asString()
                .log()
                .verifyStatus().body == "{\"status\": \"OK\"}"
        logger.info("health check ended with $result")
        return result
    }


    override fun getRole(): String? {
        val role = Unirest.get("$basePath/get_role").basicAuth(login, password).asJson()
                .log()
                .verifyStatus().body.`object`.get("role") as String?
        logger.info("Role request ended: $role")
        return role
    }

    override fun getCustomers(): List<Customer> {
        val customers = Unirest.get("$basePath/customers")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .basicAuth(login, password).asString()
                .log()
                .verifyStatus().body.parseList<Customer>()
        logger.info("Customers request ended, customers found ${customers.size}")
        return customers
    }

    override fun createCustomer(customer: Customer): Customer {
        val response = Unirest.post("$basePath/create_customer")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .basicAuth(login, password)
                .body(mapper.writeValueAsString(customer))
                .asString()
        val created = response
                .log()
                .verifyStatus().body.parse<Customer>()
        logger.info("Created customer")
        return created
    }

    override fun getCustomerId(customerLogin: String): UUID? {
        val uuidText = Unirest.get("$basePath/get_customer_id/$customerLogin")
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .basicAuth(login, password)
                .asString().verifyStatus().body
        if (uuidText.isEmpty()) return null
        val corrected = uuidText.substring(1, uuidText.lastIndex)
        val id = UUID.fromString(corrected)
        logger.info("Get customer id for login $customerLogin: $id")
        return id
    }
}