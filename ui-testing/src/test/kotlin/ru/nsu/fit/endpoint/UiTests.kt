package ru.nsu.fit.endpoint

import org.openqa.selenium.TimeoutException
import org.testng.Assert
import org.testng.annotations.Test
import ru.nsu.fit.endpoint.data.Customer
import java.nio.file.Paths


class UiTests {
    @Test
    fun `create user`() {
        val customer = Customer(null, "Donald", "Jasons", "asdhj2K", "qwerty1Q")
        val customers = LoginPage(Config.driver, Paths.get("/Users/jetbrains/IdeaProjects/ktor-testing"))
                .login("admin", "admin")
                ?.pressAddCustomer()
                ?.addCustomer(customer)
                ?.getCustomers()
        val added = customers?.find { it.firstName == "Donald" }
        Assert.assertEquals(customer, added)
    }

    @Test(dependsOnMethods = ["create user"])
    fun `multiple users in table`() {
        val customer = Customer(null, "Jasdsad", "Ijansdj", "sddhj2K", "12erty1Q")
        val size = LoginPage(Config.driver, Paths.get("/Users/jetbrains/IdeaProjects/ktor-testing"))
                .login("admin", "admin")
                ?.pressAddCustomer()
                ?.addCustomer(customer)
                ?.getCustomers()
                ?.size
        Assert.assertNotNull(size)
        Assert.assertTrue(size!! >= 2)
    }

    @Test
    fun `login with non admin role fails`() {
        Assert.assertNull(LoginPage(Config.driver, Paths.get("/Users/jetbrains/IdeaProjects/ktor-testing"))
                .login("non_admin", "non_admin_pass", true))
    }

    @Test(dependsOnMethods = ["create user"], expectedExceptions = [TimeoutException::class])
    fun `same customer rejected`() {
        val customer = Customer(null, "Donald", "Jasons", "asdhj2K", "qwerty1Q")
        LoginPage(Config.driver, Paths.get("/Users/jetbrains/IdeaProjects/ktor-testing"))
                .login("admin", "admin")
                ?.pressAddCustomer()
                ?.addCustomer(customer)
    }

    @Test(expectedExceptions = [TimeoutException::class])
    fun `name validation failed`() {
        val customer = Customer(null, "D", "Kjasdd", "sdafg2ef", "qwedrty1Q")
        LoginPage(Config.driver, Paths.get("/Users/jetbrains/IdeaProjects/ktor-testing"))
                .login("admin", "admin")
                ?.pressAddCustomer()
                ?.addCustomer(customer)
    }

    @Test(expectedExceptions = [TimeoutException::class])
    fun `password validation failed`() {
        val customer = Customer(null, "Ddslkjsd", "Kjasdda", "sdafg2ef", "sdafg2ef")
        LoginPage(Config.driver, Paths.get("/Users/jetbrains/IdeaProjects/ktor-testing"))
                .login("admin", "admin")
                ?.pressAddCustomer()
                ?.addCustomer(customer)
    }
}