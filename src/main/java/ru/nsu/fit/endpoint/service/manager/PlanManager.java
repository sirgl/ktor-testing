package ru.nsu.fit.endpoint.service.manager;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import ru.nsu.fit.endpoint.data.Plan;
import ru.nsu.fit.endpoint.service.database.DBService;

import java.util.List;
import java.util.UUID;

public class PlanManager extends ParentManager {
    public static final String NAME_LENGTH_INCORRECT = "Name's length should be more or equal 2 symbols and less or equal 128 symbols.";
    public static final String DETAILS_LENGTH_INCORRECT = "Details's length should be more or equal 1 symbols and less or equal 1024 symbols.";
    public static final String FEE_INCORRECT = "Fee should be more or equal 0 and less or equal 999999.";
    public static final String NAME_SHOULD_CONTAIN_ONLY_LETTERS = "Name should not contain number and others symbols.";
    public static final String PLAN_WITH_SUCH_NAME_ALREADY_EXIST = "Plan with name=%s already exist.";
    public static final String PLAN_NOT_EXIST = "Plan with id=%s doesn't exist.";

    public PlanManager(DBService dbService, Logger flowLog) {
        super(dbService, flowLog);
    }

    /**
     * Метод создает новый объект типа Plan. Ограничения:
     * name - длина не больше 128 символов и не меньше 2 включительно не содержит спец символов. Имена не пересекаются друг с другом;
     * /* details - длина не больше 1024 символов и не меньше 1 включительно;
     * /* fee - больше либо равно 0 но меньше либо равно 999999.
     */
    public Plan createPlan(Plan plan) {
        Validate.notNull(plan, "Argument 'plan' is null.");

        String name = plan.getName();
        Validate.notNull(name);
        Validate.isTrue(name.length() >= 2 && name.length() <= 128,
                NAME_LENGTH_INCORRECT);
        Validate.isTrue(name.matches("[a-zA-Z]+"), NAME_SHOULD_CONTAIN_ONLY_LETTERS);
        UUID planIdByName = dbService.getPlanIdByName(name);
        if (planIdByName != null) {
            throw new IllegalArgumentException(String.format(PLAN_WITH_SUCH_NAME_ALREADY_EXIST, name));
        }

        String details = plan.getDetails();
        Validate.notNull(details);
        Validate.isTrue(details.length() >= 1 && details.length() <= 1024,
                DETAILS_LENGTH_INCORRECT);

        int fee = plan.getFee();
        Validate.notNull(fee);
        Validate.isTrue(fee >= 1 && fee <= 1024,
                FEE_INCORRECT);
        return dbService.createPlan(plan);
    }

    public Plan updatePlan(Plan requestPlan) {
        Validate.notNull(requestPlan, "Argument 'requestPlan' is null.");

        Plan originalPlan = dbService.getPlanById(requestPlan.getId());
        if (originalPlan == null) {
            throw new IllegalArgumentException(String.format(PLAN_NOT_EXIST, requestPlan.getId()));
        }
        return dbService.updatePlan(requestPlan);
    }

    public void removePlan(UUID id) {
        Validate.notNull(id, "Argument 'id' is null.");
        dbService.removePlan(id);
    }

    /**
     * Метод возвращает список планов доступных для покупки.
     */
    public List<Plan> getPlans() {
        return dbService.getPlans();
    }
}
