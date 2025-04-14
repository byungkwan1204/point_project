package com.example.point;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
@SpringBootTest
public abstract class IntegrationTestSupport {

    @Autowired
    protected EntityManager entityManager;

}
