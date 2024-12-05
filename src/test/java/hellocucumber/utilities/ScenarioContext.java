package hellocucumber.utilities;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCase;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class ScenarioContext {
    private static final ThreadLocal<ScenarioContext> SCENARIO_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();
    private Scenario scenario;

    public static ScenarioContext getInstance() {
        if (SCENARIO_CONTEXT_THREAD_LOCAL.get() == null) {
            SCENARIO_CONTEXT_THREAD_LOCAL.set(new ScenarioContext());
        }
        return SCENARIO_CONTEXT_THREAD_LOCAL.get();
    }

    public void unload() {
        SCENARIO_CONTEXT_THREAD_LOCAL.remove();
    }

    public List<PickleStepTestStep> getSteps() throws NoSuchFieldException, IllegalAccessException {
        // access to scenario delegate
        Field delegate = scenario.getClass().getDeclaredField("delegate");
        delegate.setAccessible(true);
        TestCaseState testCaseState = (TestCaseState) delegate.get(scenario);

        // get test case info
        Field testCaseField = testCaseState.getClass().getDeclaredField("testCase");
        testCaseField.setAccessible(true);
        TestCase testCase = (TestCase) testCaseField.get(testCaseState);

        // get test step list
        return testCase.getTestSteps().stream()
                .filter(PickleStepTestStep.class::isInstance)
                .map(PickleStepTestStep.class::cast)
                .collect(Collectors.toList());
    }
}
