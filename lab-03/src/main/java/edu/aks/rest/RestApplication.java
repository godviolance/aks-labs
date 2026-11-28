package edu.aks.rest;

import edu.aks.catalog.CatalogApplication;
import org.springframework.boot.SpringApplication;

public class RestApplication {
    public static void main(String[] args) {
        SpringApplication.run(new Class<?>[]{CatalogApplication.class, RestModule.class}, args);
    }
}
