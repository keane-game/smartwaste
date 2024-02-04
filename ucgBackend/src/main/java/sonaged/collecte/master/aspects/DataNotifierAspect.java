package sonaged.collecte.master.aspects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sonaged.collecte.master.annotations.Notifiable;


public class DataNotifierAspect {
/***
    @Qualifier("topicJmsTemplate")
    private final JmsTemplate jmsTemplate;

    @Around("execution(@com.worldline.tapandgo.user.annotations.Notifiable * *.*(..)) && @annotation(notifiable)")
    public Object notifyData(ProceedingJoinPoint p, Notifiable notifiable) throws Throwable {
        /* Getting method output object */
        Object object = p.proceed();

        /* Getting notification parameters
        String topicName = notifiable.topicName();
        boolean isActive = notifiable.active();

        /* Checking if object is null or an empty Optional */
        if (object == null || (Optional.class.isInstance(object) && !((Optional) object).isPresent())) {

            log.warn("notifyData aborted ! - topicName: {} - isActive: {} - object: {}", topicName, isActive, object);

        } else {

            /* Checking if notification is active */
            if (isActive) {

                log.debug("notifyData start - topicName: {} - object: {}", topicName, object);

                /* Initializing objects list */
                final List<Object> objects;

                /* Checking if object is a list */
                if (ArrayList.class.isInstance(object)) {

                    /* Setting objects list */
                    objects = (List<Object>) object;

                } else if (Optional.class.isInstance(object) && ((Optional) object).isPresent()) {

                    /* Setting objects list */
                    objects = Arrays.asList(((Optional) object).get());

                } else {

                    /* Setting objects list */
                    objects = Arrays.asList(object);

                }

                log.debug("notifyData - topicName: {} - objects: {}", topicName, objects);

                /* Setting virtualTopicName */
                String virtualTopicName = new StringBuilder().append("VirtualTopic.").append(topicName).toString();

                /* Iterate over objects list */
                for (Object currentObject : objects) {

                    /* Publishing object */
                 //   jmsTemplate.convertAndSend(virtualTopicName, currentObject);

                    log.info("notifyData end ok - topicName: {}", virtualTopicName);

                }

            }

        }

        return object;

    }
***/
}
