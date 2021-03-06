package com.stratio.cucumber.aspects;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class AssertAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("call(static public void org.hamcrest.MatcherAssert.assertThat(String, Object, org.hamcrest.Matcher))"
            + " && args(reason, actual, matcher)")
    protected void logAssertFailurePointcut(String reason, Object actual, Matcher<?> matcher) {
    }
/**
 * 
 * @param pjp
 * @param reason
 * @param actual
 * @param matcher
 * @throws Throwable
 */
    @Around(value = "logAssertFailurePointcut(reason, actual, matcher)")
    public void aroundLogAssertFailurePointcut(ProceedingJoinPoint pjp, String reason, Object actual, Matcher<?> matcher)
            throws Throwable {
        try {
            pjp.proceed();
        } catch (AssertionError e) {
            logger.error("Assertion failed: {}", reason);
            if ((actual instanceof ArrayList) && (matcher.getClass().toString().endsWith("IsCollectionWithSize"))) {

                List<?> actualList = (ArrayList<?>) actual;
                if (actualList.size() > 0) {
                    Object el = actualList.get(actualList.size() - 1);
                    if (el != null && (el instanceof Exception)) {
                        logger.error("Captured exception list last entry class: '{}' and message: '{}'",
                                ((Exception) el).getClass().getSimpleName(), ((Exception) el).getMessage());
                    }
                }
            }
            throw e;
        }
    }
}