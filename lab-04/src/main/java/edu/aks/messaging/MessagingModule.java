package edu.aks.messaging;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@ComponentScan("edu.aks.messaging")
@EnableJms
@EnableScheduling
public class MessagingModule {}
