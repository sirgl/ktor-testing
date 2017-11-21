package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ObjectArrayArguments;
import org.mockito.Mockito;
import org.slf4j.Logger;
import ru.nsu.fit.endpoint.data.Customer;
import ru.nsu.fit.endpoint.service.database.DBService;
import ru.nsu.fit.endpoint.service.manager.CustomerManager;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class CustomerManagerTest {
    private DBService dbService;
    private Logger logger;
    private CustomerManager customerManager;

    private Customer customerBeforeCreateMethod;
    private Customer customerAfterCreateMethod;

    @BeforeEach
    public void before() {
        // create stubs for the test's class
        dbService = Mockito.mock(DBService.class);
        logger = Mockito.mock(Logger.class);

        customerBeforeCreateMethod = new Customer(null, "Jonson", "Wickinder", "john_wick@gmail.com", "Baba_Jaga", 0);

        customerAfterCreateMethod = customerBeforeCreateMethod.clone();
        customerAfterCreateMethod.setId(UUID.randomUUID());

        Mockito.when(dbService.createCustomer(customerBeforeCreateMethod)).thenReturn(customerAfterCreateMethod);

        // create the test's class
        customerManager = new CustomerManager(dbService, logger);
    }

    @Test
    public void testCreateNewCustomer() {
        // Вызываем метод, который хотим протестировать
        Customer customer = customerManager.createCustomer(customerBeforeCreateMethod);

        // Проверяем результат выполенния метода
        assertEquals(customer.getId(), customerAfterCreateMethod.getId());

        // Проверяем, что метод мока базы данных был вызван 1 раз
        verify(dbService, times(1)).getCustomerIdByLogin(customerBeforeCreateMethod.getLogin());
        verify(dbService, times(1)).createCustomer(customerBeforeCreateMethod);
    }

    @Test
    public void testCreateCustomerWithNullArgument() {
        try {
            customerManager.createCustomer(null);
        } catch (IllegalArgumentException ex) {
            assertEquals("Argument 'customerData' is null.", ex.getMessage());
        }
    }

    @Test
    public void testCreateCustomerWithEasyPassword() {
        try {
            customerBeforeCreateMethod.setPass("123qwe");
            customerManager.createCustomer(customerBeforeCreateMethod);
        } catch (IllegalArgumentException ex) {
            assertEquals("Password is easy.", ex.getMessage());
        }
    }

    @ParameterizedTest()
    @MethodSource(names = "getIncorrectFirstNames")
    void testCreateCustomerWithIncorrectFirsName(String firstName, String errorMessage) {
        customerBeforeCreateMethod.setFirstName(firstName);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerManager.createCustomer(customerBeforeCreateMethod));

        assertEquals(errorMessage, exception.getMessage());
    }

    private static Stream<Arguments> getIncorrectFirstNames() {
        return Stream.of(
                ObjectArrayArguments.create("jonson", CustomerManager.FIRST_NAME_SHOULD_START_WITH_UPPER_CASE),
                ObjectArrayArguments.create("JONSON", CustomerManager.FIRST_NAME_CASE_INCORRECT),
                ObjectArrayArguments.create("Verylongtestname", CustomerManager.FIRST_NAME_LENGTH_INCORRECT),
                ObjectArrayArguments.create("a", CustomerManager.FIRST_NAME_LENGTH_INCORRECT),
                ObjectArrayArguments.create("David12345", CustomerManager.FIRST_NAME_SHOULD_CONTAIN_ONLY_LETTERS),
                ObjectArrayArguments.create("David-star", CustomerManager.FIRST_NAME_SHOULD_CONTAIN_ONLY_LETTERS));
    }

    @Test
    void testCreateCustomerWithIncorrectBalance() {
        customerBeforeCreateMethod.setBalance(1000);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerManager.createCustomer(customerBeforeCreateMethod));

        assertEquals(CustomerManager.BALANCE_INCORRECT, exception.getMessage());
    }

    @ParameterizedTest()
    @MethodSource(names = "getIncorrectLastNames")
    void testCreateCustomerWithIncorrectLastName(String lastName, String errorMessage) {
        customerBeforeCreateMethod.setLastName(lastName);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerManager.createCustomer(customerBeforeCreateMethod));

        assertEquals(errorMessage, exception.getMessage());
    }

    private static Stream<Arguments> getIncorrectLastNames() {
        return Stream.of(
                ObjectArrayArguments.create("jonson", CustomerManager.LAST_NAME_SHOULD_START_WITH_UPPER_CASE),
                ObjectArrayArguments.create("JONSON", CustomerManager.LAST_NAME_CASE_INCORRECT),
                ObjectArrayArguments.create("Verylongtestname", CustomerManager.LAST_NAME_LENGTH_INCORRECT),
                ObjectArrayArguments.create("a", CustomerManager.LAST_NAME_LENGTH_INCORRECT),
                ObjectArrayArguments.create("David12345", CustomerManager.LAST_NAME_SHOULD_CONTAIN_ONLY_LETTERS),
                ObjectArrayArguments.create("David-star", CustomerManager.LAST_NAME_SHOULD_CONTAIN_ONLY_LETTERS));
    }


}
