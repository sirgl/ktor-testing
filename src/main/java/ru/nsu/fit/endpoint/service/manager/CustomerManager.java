package ru.nsu.fit.endpoint.service.manager;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import ru.nsu.fit.endpoint.data.Customer;
import ru.nsu.fit.endpoint.service.database.DBService;

import java.util.List;
import java.util.UUID;

public class CustomerManager extends ParentManager {

    public static final String EMAIL_PATTERN = "^([_a-zA-Z0-9-]+(\\\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\\\.[a-zA-Z0-9-]+)*(\\\\.[a-zA-Z]{1,6}))?$";
    public static final String FIRST_NAME_LENGTH_INCORRECT = "First name's length should be more or equal 6 symbols and less or equal 12 symbols.";
    public static final String LAST_NAME_LENGTH_INCORRECT = "Last name's length should be more or equal 6 symbols and less or equal 12 symbols.";
    public static final String FIRST_NAME_SHOULD_START_WITH_UPPER_CASE = "First name should start with upper case symbol.";
    public static final String LAST_NAME_SHOULD_START_WITH_UPPER_CASE = "Last name should start with upper case symbol.";
    public static final String FIRST_NAME_CASE_INCORRECT = "All first name's symbols except first should be lower case.";
    public static final String LAST_NAME_CASE_INCORRECT = "All last name's symbols except first should be lower case.";
    public static final String FIRST_NAME_SHOULD_CONTAIN_ONLY_LETTERS = "First name should not contain number and others symbols.";
    public static final String LAST_NAME_SHOULD_CONTAIN_ONLY_LETTERS = "Last name should not contain number and others symbols.";
    public static final String BALANCE_INCORRECT = "For new customer balance should be 0";
    public static final String AMOUNT_INCORRECT = "Amount should be positive.";
    public static final String CUSTOMER_NOT_EXIST = "Customer with id=%s doesn't exist.";

    public CustomerManager(DBService dbService, Logger flowLog) {
        super(dbService, flowLog);
    }

    /**
     * Метод создает новый объект типа Customer. Ограничения:
     * Аргумент 'customerData' - не null;
     * firstName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * lastName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * login - указывается в виде email, проверить email на корректность, проверить что нет customer с таким же email;
     * pass - длина от 6 до 12 символов включительно, не должен быть простым (123qwe или 1q2w3e), не должен содержать части login, firstName, lastName
     * balance - должно быть равно 0.
     */
    public Customer createCustomer(Customer customer) {
        Validate.notNull(customer, "Argument 'customerData' is null.");

        String firstName = customer.getFirstName();
        Validate.notNull(firstName);
        Validate.isTrue(firstName.length() >= 6 && firstName.length() < 13,
                FIRST_NAME_LENGTH_INCORRECT);
        Validate.isTrue(Character.isUpperCase(firstName.charAt(0)), FIRST_NAME_SHOULD_START_WITH_UPPER_CASE);
        Validate.isTrue(firstName.substring(1).equals(firstName.substring(1).toLowerCase()),
                FIRST_NAME_CASE_INCORRECT);
        Validate.isTrue(firstName.matches("[a-zA-Z]+"), FIRST_NAME_SHOULD_CONTAIN_ONLY_LETTERS);

        String lastName = customer.getLastName();
        Validate.notNull(lastName);
        Validate.isTrue(lastName.length() >= 6 && lastName.length() < 13,
                LAST_NAME_LENGTH_INCORRECT);
        Validate.isTrue(Character.isUpperCase(lastName.charAt(0)), LAST_NAME_SHOULD_START_WITH_UPPER_CASE);
        Validate.isTrue(lastName.substring(1).equals(lastName.substring(1).toLowerCase()),
                LAST_NAME_CASE_INCORRECT);
        Validate.isTrue(lastName.matches("[a-zA-Z]+"), LAST_NAME_SHOULD_CONTAIN_ONLY_LETTERS);

        String login = customer.getLogin();
        Validate.notNull(login);
        Validate.isTrue(!login.matches(EMAIL_PATTERN), "Login should be valid email.");
        UUID customerIdByLogin = dbService.getCustomerIdByLogin(login);
        if (customerIdByLogin != null) {
            throw new IllegalArgumentException("A customer with login = " + login + " already exist.");
        }

        String password = customer.getPass();
        Validate.notNull(password);
        Validate.isTrue(password.length() >= 6 && password.length() < 13,
                "Password's length should be more or equal 6 symbols and less or equal 12 symbols.");
        Validate.isTrue(!password.equalsIgnoreCase("123qwe"), "Password is easy.");
        Validate.isTrue(!password.equalsIgnoreCase("1q2w3e"), "Password is easy.");
        Validate.isTrue(!password.contains(login), "Password should not contain login.");
        Validate.isTrue(!password.contains(firstName), "Password should not contain firsName.");
        Validate.isTrue(!password.contains(lastName), "Password should not contain lastName.");

        int balance = customer.getBalance();
        Validate.isTrue(balance == 0, BALANCE_INCORRECT);

        return dbService.createCustomer(customer);
    }

    /**
     * Метод возвращает список объектов типа customer.
     */
    public List<Customer> getCustomers() {
        return dbService.getCustomers();
    }



    /**
     * Метод обновляет объект типа Customer.
     * Можно обновить только firstName и lastName.
     */
    public Customer updateCustomer(Customer requestCustomer) {
        Validate.notNull(requestCustomer, "Argument 'requestCustomer' is null.");

        Customer originalCustomer = dbService.getCustomerById(requestCustomer.getId());
        if (originalCustomer == null) {
            throw new IllegalArgumentException(String.format(CUSTOMER_NOT_EXIST, requestCustomer.getId()));
        }

        if (!requestCustomer.equalsIgnoringFirstAndLastName(originalCustomer)) {
            throw new IllegalArgumentException("Only firstName and lastName can be changed.");
        }

        return dbService.updateCustomer(requestCustomer);
    }

    public void removeCustomer(UUID id) {
        dbService.removeCustomer(id);
    }

    /**
     * Метод добавляет к текущему баласу amount.
     * amount - должен быть строго больше нуля.
     */
    public Customer topUpBalance(UUID customerId, int amount) {
        Validate.isTrue(amount > 0, AMOUNT_INCORRECT);
        Validate.notNull(customerId);

        Customer customer = dbService.getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException(String.format(CUSTOMER_NOT_EXIST, customerId));
        }
        customer.setBalance(customer.getBalance() + amount);

        return dbService.updateCustomer(customer);
    }
}
