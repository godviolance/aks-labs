package edu.aks.messaging;

import edu.aks.catalog.CatalogApplication;
import edu.aks.rest.RestModule;
import org.springframework.boot.SpringApplication;

public class MessagingApplication {
    public static void main(String[] args) {
        SpringApplication.run(new Class<?>[]{CatalogApplication.class, RestModule.class, MessagingModule.class}, args);
    }
}
