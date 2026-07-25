package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByEmail - encuentra el usuario guardado por su email")
    void findByEmail_usuarioExistente_loEncuentra() {
        User user = new User();
        user.setUsername("alex");
        user.setEmail("alex@vodeo.com");
        user.setPassword("hash-bcrypt");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("alex@vodeo.com");

        assertTrue(found.isPresent());
        assertEquals("alex", found.get().getUsername());
        assertEquals(User.Role.USER, found.get().getRole());
    }

    @Test
    @DisplayName("save - email duplicado viola la restriccion unique de la base de datos")
    void save_emailDuplicado_lanzaExcepcion() {
        User user1 = new User();
        user1.setUsername("alex");
        user1.setEmail("duplicado@vodeo.com");
        user1.setPassword("hash1");
        entityManager.persistAndFlush(user1);

        User user2 = new User();
        user2.setUsername("otro");
        user2.setEmail("duplicado@vodeo.com");
        user2.setPassword("hash2");

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user2));
    }
}