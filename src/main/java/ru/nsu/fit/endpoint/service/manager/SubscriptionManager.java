package ru.nsu.fit.endpoint.service.manager;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import ru.nsu.fit.endpoint.data.Customer;
import ru.nsu.fit.endpoint.data.Plan;
import ru.nsu.fit.endpoint.data.Subscription;
import ru.nsu.fit.endpoint.service.database.DBService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubscriptionManager extends ParentManager {
    public SubscriptionManager(DBService dbService, Logger flowLog) {
        super(dbService, flowLog);
    }

    /**
     * Метод создает подписку. Ограничения:
     * 1. Подписки с таким планом пользователь не имеет.
     * 2. Стоймость подписки не превышает текущего баланса кастомера и после покупки вычитается из его баласа.
     */
    public Subscription createSubscription(Subscription subscription) {
        Validate.notNull(subscription, "Argument 'subscription' is null.");

        UUID customerId = subscription.getCustomerId();
        Validate.notNull(subscription, "Argument 'subscription.customerId' is null.");
        List<Subscription> subscriptionsByCustomerId = dbService.getSubscriptionsByCustomerId(customerId);
        UUID requestPlanId = subscription.getPlanId();
        Validate.notNull(requestPlanId, "Argument 'subscription.planId' is null.");
        Optional<Subscription> subscriptionWithRequestPlan = subscriptionsByCustomerId.stream()
                .filter(s -> {
                    return s.getPlanId().equals(requestPlanId);
                })
                .findFirst();
        if (subscriptionWithRequestPlan.isPresent()) {
            throw new IllegalArgumentException("Subsciption with plan=" + requestPlanId + " already exist.");
        }

        Customer customerById = dbService.getCustomerById(customerId);
        Validate.notNull(customerById, "Customer with id=" + customerId+ "doesn't exist.");
        Plan planById = dbService.getPlanById(requestPlanId);
        Validate.notNull(planById, "Plan with id=" + requestPlanId+ "doesn't exist.");

        if (customerById.getBalance() < planById.getFee()) {
            throw new IllegalArgumentException("Customer don't have enough money.");
        }

        return dbService.createSubscription(subscription);
    }

    public void removeSubscription(UUID id) {
        Validate.notNull(id, "Argument 'id' is null.");
        dbService.removeSubscription(id);
    }

    /**
     * Возвращает список подписок указанного customer'а.
     */
    public List<Subscription> getSubscriptions(UUID id) {
        Validate.notNull(id, "Argument 'id' is null.");
        return dbService.getSubscriptionsByCustomerId(id);
    }
}
