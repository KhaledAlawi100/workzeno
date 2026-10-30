package com.workzeno.workzeno.shared.aop;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Around("execution(* com.workzeno.workzeno..service..*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint)
            throws Throwable {

        String className = joinPoint.getTarget()
                .getClass()
                .getSimpleName();

        String methodName = joinPoint.getSignature()
                .getName();

        long startTime = System.nanoTime();

        log.info("{}.{} started", className, methodName);

        try {
            Object result = joinPoint.proceed();

            long duration = (System.nanoTime() - startTime) / 1_000_000;

            log.info(
                    "{}.{} completed in {} ms",
                    className,
                    methodName,
                    duration
            );

            return result;

        } catch (Throwable exception) {

            long duration = (System.nanoTime() - startTime) / 1_000_000;

            log.error(
                    "{}.{} failed after {} ms. Exception: {}",
                    className,
                    methodName,
                    duration,
                    exception.getClass().getSimpleName()
            );

            throw exception;
        }
    }
}