package edu.aks.messaging;

import org.springframework.context.annotation.ComponentScan;
import edu.aks.messaging.configuration.CatalogMailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@ComponentScan("edu.aks.messaging")
@EnableConfigurationProperties(CatalogMailProperties.class)
@EnableJms
@EnableScheduling
public class MessagingModule {}
