package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ObjectArrayArguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import ru.nsu.fit.endpoint.data.Plan;
import ru.nsu.fit.endpoint.service.database.DBService;
import ru.nsu.fit.endpoint.service.manager.PlanManager;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PlanManagerTest {
    public static final String STRING_WITH_128_LENGHT = "zbBKCaivEugLLrONPJmvufHxkXetoWXsssGBsdITgxvfhVNTnrdqqELRrMEkUknuqQabMLwXUAKzoMkcFgMgyavzIWeEyBJGwIsYriOApuSwVcVjoXwwaohoPFkJPiw";
    private DBService dbService;
    private Logger logger;
    private PlanManager planManager;

    private Plan planBeforeCreateMethod;
    private Plan planAfterCreateMethod;

    @BeforeEach
    public void before() {
        // create stubs for the test's class
        dbService = Mockito.mock(DBService.class);
        logger = Mockito.mock(Logger.class);

        planBeforeCreateMethod = new Plan(null, "Jonson", "Wickinder", 42);
        planAfterCreateMethod = planBeforeCreateMethod.clone();
        planAfterCreateMethod.setId(UUID.randomUUID());

        Mockito.when(dbService.createPlan(planBeforeCreateMethod)).thenReturn(planAfterCreateMethod);

        // create the test's class
        planManager = new PlanManager(dbService, logger);
    }

    @Test
    void testCreatePlan() {
        // Вызываем метод, который хотим протестировать
        Plan plan = planManager.createPlan(planBeforeCreateMethod);

        // Проверяем результат выполенния метода
        assertEquals(plan.getId(), planAfterCreateMethod.getId());

        // Проверяем, что метод мока базы данных был вызван 1 раз
        verify(dbService, times(1)).getPlanIdByName(planBeforeCreateMethod.getName());
        verify(dbService, times(1)).createPlan(planBeforeCreateMethod);
    }

    @ParameterizedTest
    @MethodSource(names = "getIncorrectNames")
    void testCreatePlanWithIncorrectName(String errorMessage, String name) {
        planBeforeCreateMethod.setName(name);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> planManager.createPlan(planBeforeCreateMethod));

        assertEquals(errorMessage, exception.getMessage());
    }

    private static Stream<Arguments> getIncorrectNames() {

        return Stream.of(
                ObjectArrayArguments.create(PlanManager.NAME_LENGTH_INCORRECT, "a"),
                ObjectArrayArguments.create(PlanManager.NAME_LENGTH_INCORRECT,
                        "lg7l4aL9C17EbIVHfrhhEtuYla0jCaUvwh30bH9g9NnI0Z6OwRfad6Hqer2GtZefYUtifUlwfbsIb0Pjub87ckCznxYfWmPLEGUmIowtP4pfDphoD11KKLo7h9WnbOSla"),
                ObjectArrayArguments.create(PlanManager.NAME_SHOULD_CONTAIN_ONLY_LETTERS, "*abcde")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"aa", STRING_WITH_128_LENGHT})
    void testCreatePlanWithBoundaryNameValues(String name) {
        planBeforeCreateMethod.setName(name);
        Plan plan = planManager.createPlan(planBeforeCreateMethod);

        assertEquals(plan.getId(), planAfterCreateMethod.getId());
    }

    @Test
    void testCreatePlaneIfNameAlreadyExist() {
        String name = planBeforeCreateMethod.getName();
        when(dbService.getPlanIdByName(name)).thenReturn(UUID.randomUUID());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> planManager.createPlan(planBeforeCreateMethod));

        assertEquals(String.format(PlanManager.PLAN_WITH_SUCH_NAME_ALREADY_EXIST, name), exception.getMessage());
    }
}
