package sn.smartwaste.collect.config.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class SleuthTraceJmsListener {

    /**
     *  Wrap JmsListener to log exception with sleuth traces
     *
     *  The initial behaviour of sleuth tracer is broken when an exception is thrown in a JmsListener
     *  This proxy around the method call allow to bypass the issue
     *  related spring issue : https://github.com/spring-projects/spring-framework/issues/19247
     */
  //  @Around("@annotation(org.springframework.jms.annotation.JmsListener)")
    public Object traceJmsListener(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error("JmsListener error", e);
            throw e;
        }
    }
}
