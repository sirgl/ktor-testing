package ru.nsu.fit.endpoint

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import ru.nsu.fit.client.EndpointClient
import ru.nsu.fit.client.RestClient
import ru.nsu.fit.client.UnauthorizedException
import ru.nsu.fit.endpoint.data.Customer
import ru.yandex.qatools.allure.annotations.Description
import ru.yandex.qatools.allure.annotations.Features
import ru.yandex.qatools.allure.annotations.Severity
import ru.yandex.qatools.allure.model.SeverityLevel


class EndpointTest {
    private val adminClient : EndpointClient = RestClient(
            "http://localhost:8080",
            "admin",
            "admin"
    )

    private val unknownClient : EndpointClient = RestClient(
            "http://localhost:8080",
            "name",
            "pass"
    )

    @Description("Health check successfully ends")
    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Features("Customer feature")
    fun `health check exists`() {
        assertTrue(unknownClient.getHealthCheck())
    }

    @Test fun `admin role in get role`() {
        assertEquals(adminClient.getRole(), "admin")
    }

    @Test fun `unknown role in get role`() {
        assertEquals(unknownClient.getRole(), "unknown")
    }

    @Test fun `admin has access to customers endpoint`() {
        adminClient.getCustomers()
    }

    @Test(expectedExceptions = arrayOf(UnauthorizedException::class))
    fun `unknown has no access to customers endpoint`() {
        unknownClient.getCustomers()
    }

    @Test fun `created person id can be found`() {
        val customer = Customer(firstName = "Aaaaaa", lastName = "Bbbbbb", login = "a123456", pass = "123456")
        val createdCustomer = adminClient.createCustomer(customer)
        val customerId = adminClient.getCustomerId("a123456")
        assertEquals(customerId, createdCustomer.id)
        adminClient.removeCustomer(createdCustomer.id ?: throw IllegalStateException())
    }

    @Test(expectedExceptions = arrayOf(UnauthorizedException::class))
    fun `unknown has no access to create customer`() {
        unknownClient.createCustomer(Customer())
    }

    @Test fun `created customer available in list`() {
        val createdCustomer = adminClient.createCustomer(Customer(firstName = "Qqqqqq", lastName = "Wwwwww", pass = "1234563", login = "asdfgh1"))
        val customers = adminClient.getCustomers()
        assertTrue(customers.any { it.firstName == "Qqqqqq"})
        adminClient.removeCustomer(createdCustomer.id ?: throw IllegalStateException())
    }
}