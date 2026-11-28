package edu.aks.rest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan({"edu.aks.rest.controller", "edu.aks.rest.service", "edu.aks.rest.configuration"})
public class RestModule {}
