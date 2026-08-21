package com.tander.flowable.client.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Aspect
public class GlobalLoggingAspect {

    @AfterThrowing(pointcut = "@annotation(LogErrors)", throwing = "ex")
    public void logServiceExceptions(JoinPoint jp, Throwable ex) {
        log.error("Ошибка в {}.{}: {}",
            jp.getTarget().getClass().getSimpleName(),
            jp.getSignature().getName(),
            ex.getMessage(), ex);
    }

    @Around("@annotation(LogErrors)")
    public Object logErrorAround(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            log.error("Метод {} упал с исключением: {}",
                pjp.getSignature().toShortString(), ex.getMessage(), ex);
            throw ex; // пробрасываем дальше
        }
    }

}
