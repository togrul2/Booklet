package com.github.togrul2.booklet.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Profile("dev")
public class TimerAspect {
    private Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            long start = System.currentTimeMillis();
            Object proceed = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            log.info("{} with arguments {} executed in {}ms", joinPoint.getSignature(), joinPoint.getArgs(),
                    executionTime);
            return proceed;
        } catch (Throwable throwable) {
            log.error("Error occurred in {}", joinPoint.getSignature());
            throw throwable;
        }
    }

    @Pointcut("execution(* com.github.togrul2.booklet.services.*.*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logServiceMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint);
    }

    @Around("@annotation(com.github.togrul2.booklet.annotations.LogExecutionTime)")
    public Object logAnnotatedMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logExecutionTime(joinPoint);
    }
}
