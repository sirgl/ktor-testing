package ru.nsu.fit.endpoint

import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.Reporter
import ru.nsu.fit.endpoint.data.Customer
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class ReportingLogger {
    private val logger: Logger = LoggerFactory.getLogger("Ui")

    fun debug(text: String) {
        Reporter.log(text + "\n")
        logger.debug(text)
    }
}

val logger = ReportingLogger()

abstract class Page(val driver: WebDriver, private val screenDirectory: Path) {
    abstract val address: String
    val driverWait: WebDriverWait = WebDriverWait(driver, 2)

    private fun makeScreenshot(): ByteArray {
        val tmpFile = (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
        return Files.readAllBytes(Paths.get(tmpFile.path))
    }

    fun saveScreen() {
        val dirPath = screenDirectory.resolve(address)
        Files.createDirectories(dirPath)
        val filePath = dirPath.resolve(UUID.randomUUID().toString() + ".png")
        val file = filePath.toFile()
        FileOutputStream(file).write(makeScreenshot())
        Reporter.log("<img src=\"file://${filePath.toAbsolutePath()}\">")
    }
}

object Config {
    val driver = ChromeDriver()
}

class LoginPage(driver: WebDriver, private val screenDir: Path) : Page(driver, screenDir) {
    override val address = "login"

    init {
        driver.get("http://localhost:8080/login.html")
        driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("login")))
    }

    fun login(login: String, password: String, bad : Boolean = false) : CustomersPage? {
        logger.debug("Trying login with login $login and password $password")
        driver.findElement(By.id("email")).sendKeys(login)
        driver.findElement(By.id("password")).sendKeys(password)
        saveScreen()
        driver.findElement(By.id("login")).click()
        if(bad) {
            driverWait.until(ExpectedConditions.alertIsPresent())
            logger.debug("Alert appeared, failed to login")
            driver.switchTo().alert().accept()
            return null
        }
        return CustomersPage(driver, screenDir)
    }
}

class AddCustomerPage(driver: WebDriver, private val screenDir: Path) : Page(driver, screenDir) {
    override val address = "add_customer"

    init {
        driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("create_customer_id")))
    }

    fun addCustomer(customer: Customer) : CustomersPage? {
        logger.debug("Adding new customer: $customer")
        driver.findElement(By.id("first_name_id")).sendKeys(customer.firstName)
        driver.findElement(By.id("last_name_id")).sendKeys(customer.lastName)
        driver.findElement(By.id("email_id")).sendKeys(customer.login)
        driver.findElement(By.id("password_id")).sendKeys(customer.pass)
        saveScreen()
        driver.findElement(By.id("create_customer_id")).click()
        return CustomersPage(driver, screenDir)
    }
}

class CustomersPage(driver: WebDriver, private val screenDir: Path) : Page(driver, screenDir) {
    override val address = "customers"

    init {
        driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("add_new_customer")))
    }

    fun pressAddCustomer() : AddCustomerPage {
        logger.debug("Clicking to button 'add new customer'")
        saveScreen()
        driver.findElement(By.id("add_new_customer")).click()
        return AddCustomerPage(driver, screenDir)
    }

    fun getCustomers() : List<Customer> {
        return driver.findElements(By.tagName("tr"))
                .map { it.findElements(By.tagName("td")) }
                .filter{ it.size == 5}
                .map { Customer(null, it[0].text, it[1].text, it[2].text, it[3].text, it[4].text.toInt()) }
    }
}