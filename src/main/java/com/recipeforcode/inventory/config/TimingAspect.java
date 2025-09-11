package com.recipeforcode.inventory.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class TimingAspect {

    @Around("within(com.recipeforcode.inventory.service..*)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.nanoTime();
        try {
            Object res = pjp.proceed();
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            log.info("OK {} took {} ms", pjp.getSignature(), ms);
            return res;
        } catch (Throwable e) {
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
            log.error("ERR {} after {} ms: {}", pjp.getSignature(), ms, e.toString());
            throw e;
        }
    }
}
