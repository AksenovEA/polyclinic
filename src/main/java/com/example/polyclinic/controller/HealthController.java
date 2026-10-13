package com.example.polyclinic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public String health()
    {
        try(Connection connection = dataSource.getConnection())
        {
            return "It Works " + connection.getCatalog();
        }
        catch (SQLException exception)
        {
            return exception.getSQLState();
        }
    }
}
