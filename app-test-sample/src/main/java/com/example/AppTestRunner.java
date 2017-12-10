package com.example;

import com.willowtreeapps.apptest.Config;
import com.willowtreeapps.apptest.Driver;
import com.willowtreeapps.apptest.FormFactor;
import com.willowtreeapps.apptest.Platform;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Constructor;
import java.util.List;

public class AppTestRunner extends BlockJUnit4ClassRunner {

    private Driver driver;
    private Platform platform;
    private FormFactor formFactor;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public AppTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        driver = Driver.start("localhost", Config.fromResource("/apptest.properties"));
    }

    @Override
    public void run(RunNotifier notifier) {
        driver.getAppController().setup();
        platform = driver.getPlatform();
        formFactor = driver.getFormFactor();
        super.run(notifier);
        driver.getAppController().shutdown();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        driver.getAppController().startApp();
        super.runChild(method, notifier);
        driver.getAppController().stopApp();
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        RunOn runOn = child.getAnnotation(RunOn.class);
        if (runOn == null) {
            runOn = child.getDeclaringClass().getAnnotation(RunOn.class);
        }
        if (runOn == null) {
            return false;
        }
        boolean shouldRun = true;
        switch (runOn.platform()) {
            case NONE:
                shouldRun &= false;
                break;
            case ALL:
                shouldRun &= true;
                break;
            default:
                shouldRun &= runOn.platform() == platform;
                break;
        }
        switch (runOn.formFactor()) {
            case NONE:
                shouldRun &= false;
                break;
            case ALL:
                shouldRun &= true;
                break;
            default:
                shouldRun &= runOn.formFactor() == formFactor;
                break;
        }
        return !shouldRun;
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        return super.createTestClass(testClass);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
        validateSingleDriverArgConstructor(errors);
    }

    private void validateSingleDriverArgConstructor(List<Throwable> errors) {
        if (!getTestClass().isANonStaticInnerClass()) {
            if (getTestClass().getOnlyConstructor().getParameterTypes().length != 1) {
                String gripe = "Test class should have exactly one public single-argument constructor";
                errors.add(new Exception(gripe));
            } else if (!getTestClass().getOnlyConstructor().getParameterTypes()[0].equals(Driver.class)) {
                String gripe = "Test class constructor argument must be of type Driver";
                errors.add(new Exception(gripe));
            }
        }
    }

    @Override
    protected Object createTest() throws Exception {
        Constructor<?> constructor = getTestClass().getOnlyConstructor();
        return constructor.newInstance(driver);
    }
}
