/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.testng;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.testng.TestingMessageHelper.methodCount;
import static org.testng.TestingMessageHelper.reporterAttached;
import static org.testng.TestingMessageHelper.rootPresentation;
import static org.testng.TestingMessageHelper.testFailed;
import static org.testng.TestingMessageHelper.testFinished;
import static org.testng.TestingMessageHelper.testIgnored;
import static org.testng.TestingMessageHelper.testStarted;
import static org.testng.TestingMessageHelper.testSuiteFinished;

/**
 *
 */
public class CheTestNGListener {

    private final PrintStream out;
    private final List<String> currentSuites = new ArrayList<>();
    private final Map<TestResultWrapper, TestResultWrapper> testResults = new HashMap<>();
    private final Map<String, Integer> invocationCounts = new HashMap<>();
    private final Map<TestResultWrapper, String> parametersMap = new HashMap<>();


    public CheTestNGListener() {
        out = System.out;
        reporterAttached(out);
    }

    public void onSuiteStart(ISuite suite) {
        if (suite != null) {
            try {

                List<ITestNGMethod> allMethods = suite.getAllMethods();
                if (allMethods != null) {
                    int count = 0;
                    for (ITestNGMethod method : allMethods) {
                        if (method.isTest()) {
                            count += method.getInvocationCount();
                        }
                    }
                    methodCount(out, count);
                }
            } catch (NoSuchMethodError ignore) {
            }
            rootPresentation(out, suite.getName(), suite.getXmlSuite().getFileName());
        }
    }

    public void onSuiteFinish(ISuite suite) {
        try {
            if (suite != null && suite.getAllInvokedMethods().size() < suite.getAllMethods().size()) {
                for (ITestNGMethod ngMethod : suite.getAllMethods()) {
                    if (ngMethod.isTest()) {
                        boolean methodInvoked = false;
                        for (IInvokedMethod invokedMethod : suite.getAllInvokedMethods()) {
                            if (invokedMethod.getTestMethod() == ngMethod) {
                                methodInvoked = true;
                                break;
                            }
                        }

                        if (!methodInvoked) {
                            String methodName = shortName(ngMethod.getTestClass().getName()) + "." + ngMethod.getMethodName();
                            testStarted(out, methodName);
                            testIgnored(out, methodName);
                            testFinished(out, methodName);
                            break;
                        }
                    }
                }
            }


        } catch (NoSuchMethodError ignored) {
        }

        for (int i = currentSuites.size() - 1; i >= 0; i--) {
            internalSuiteFinish(currentSuites.get(i));
        }
        currentSuites.clear();

    }

    private void internalSuiteFinish(String suite) {
        TestingMessageHelper.testSuiteFinished(out, suite);
    }

    private String shortName(String name) {
        int lastPoint = name.lastIndexOf('.');
        if (lastPoint >= 0) {
            return name.substring(lastPoint + 1);
        }
        return name;
    }

    public void onTestStart(ITestResult result) {
        internalOnTestStart(createWrapper(result));
    }

    private void internalOnTestStart(TestResultWrapper wrapper) {
        Object[] parameters = wrapper.getParameters();
        String fqn = wrapper.getClassName() + wrapper.getDisplayMethodName();
        Integer count = invocationCounts.get(fqn);
        if (count == null) {
            count = 0;
        }

        String paramStr = getParametersStr(parameters, count);

        internalOnTestStart(wrapper, paramStr, count, false);
        invocationCounts.put(fqn, ++count);
    }

    private void internalOnTestStart(TestResultWrapper wrapper, String paramStr, Integer count, boolean config) {
        parametersMap.put(wrapper, paramStr);
        internalOnSuiteStart(wrapper, wrapper.getTestHierarchy(), true);

        String location = wrapper.getClassName() + "." + wrapper.getMethodName() + (count >= 0 ? "[" + count + "]" : "");
        testStarted(out, shortName(wrapper.getClassName() + "." + wrapper.getMethodName() + (paramStr != null ? paramStr : "")), location, config);
    }

    private void internalOnSuiteStart(TestResultWrapper wrapper, List<String> testHierarchy, boolean provideLocation) {
        int index = 0;
        String currentClass, currentParent;

        while (index < currentSuites.size() && index < testHierarchy.size()) {
            currentClass = currentSuites.get(index);
            currentParent = testHierarchy.get(testHierarchy.size() - 1 - index);
            if (!currentClass.equals(shortName(currentParent))) {
                break;
            }
            index++;
        }

        for (int i = currentSuites.size() - 1; i >= 0; i--) {
            currentClass = currentSuites.remove(i);
            testSuiteFinished(out, currentClass);
        }

        for (int i = index; i < testHierarchy.size(); i++) {
            String testName = testHierarchy.get(testHierarchy.size() - 1 - i);
            String currentClassName = shortName(testName);
            String location = "java:suite://" + testName;
            if (wrapper != null) {
                String xmlTestName = wrapper.getXmlTestName();
                if (testName.equals(xmlTestName)) {
                    String fileName = wrapper.getFileName();
                    if (fileName != null) {
                        location = "file://" + fileName;
                    }
                }
            }
            TestingMessageHelper.testSuiteStarted(out, currentClassName, location, provideLocation);
            currentSuites.add(currentClassName);
        }

    }

    private String getParametersStr(Object[] parameters, Integer count) {
        String paramStr = "";
        if (parameters.length > 0) {
            StringJoiner joiner = new StringJoiner(", ");
            for (Object parameter : parameters) {
                joiner.add(parameter.toString());
            }

            paramStr = "[" + joiner.toString() + "]";
        }
        if (count > 0) {
            paramStr += " (" + count + ")";
        }

        return paramStr.isEmpty() ? null : paramStr;
    }

    private TestResultWrapper createWrapper(ITestResult result) {
        TestResultWrapper wrapper = new TestResultWrapper(result);
        TestResultWrapper oldWrapper = testResults.get(wrapper);
        if (oldWrapper != null) {
            return oldWrapper;
        }

        testResults.put(wrapper, wrapper);
        return wrapper;
    }

    public void onTestSuccess(ITestResult result) {
        onTestFinished(createWrapper(result));
    }

    private void onTestFinished(TestResultWrapper wrapper) {
        long duration = wrapper.getDuration();
        testFinished(out, getTestMethodName(wrapper), duration);
    }

    private String getTestMethodName(TestResultWrapper wrapper) {
        String testMethodName = shortName(wrapper.getMethodName())+"."+ wrapper.getDisplayMethodName();
        String paramStr = parametersMap.get(wrapper);
        if (paramStr != null) {
            testMethodName += paramStr;
        }
        return testMethodName;
    }

    public void onTestFailure(ITestResult result) {
        internalOnTestFailure(createWrapper(result));
    }

    private void internalOnTestFailure(TestResultWrapper wrapper) {
        if (!parametersMap.containsKey(wrapper)) {
            internalOnTestStart(wrapper);
        }

        Throwable throwable = wrapper.getThrowable();
        String testMethodName = getTestMethodName(wrapper);
        Map<String, String> params = new HashMap<>();
        params.put("name", testMethodName);
        if (throwable != null) {
            String failMessage = throwable.getMessage();
            //TODO add message replacement with 'Expected[] but Found[]'
            params.put("message", failMessage);
        } else {
            params.put("message", "");
        }

        out.println();
        testFailed(out, params);
    }

    public void onTestSkipped(ITestResult result) {
        internalOnTestSkipped(createWrapper(result));
    }

    private void internalOnTestSkipped(TestResultWrapper wrapper) {
        if (!parametersMap.containsKey(wrapper)) {
            internalOnTestStart(wrapper);
        }
        testIgnored(out, getTestMethodName(wrapper));
        onTestFinished(wrapper);

    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            throwable.printStackTrace(); //we catch all std.out in wsaget JVM and attach output to the output console
        }
        onTestSuccess(result);
    }

    public void onConfigurationStart(ITestResult testResult) {
        TestResultWrapper wrapper = createWrapper(testResult);
        internalOnTestStart(wrapper, null, -1, true);
    }

    public void onConfigurationSuccess(ITestResult result, boolean start) {
        TestResultWrapper wrapper = createWrapper(result);
        if (start) {
            internalOnConfigurationStart(wrapper);
        }
        internalOnConfigurationSuccess(wrapper);
    }

    private void internalOnConfigurationSuccess(TestResultWrapper wrapper) {
        onTestFinished(wrapper);
    }

    private void internalOnConfigurationStart(TestResultWrapper wrapper) {
        internalOnTestStart(wrapper, null, -1, true);
    }

    public void onConfigurationFailure(ITestResult result, boolean start) {
        TestResultWrapper wrapper = createWrapper(result);
        if (start) {
            internalOnConfigurationStart(wrapper);
        }
        internalOnTestFailure(wrapper);
    }
}
