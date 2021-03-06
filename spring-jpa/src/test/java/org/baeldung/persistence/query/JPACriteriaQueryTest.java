package org.baeldung.persistence.query;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.baeldung.config.PersistenceJPAConfig;
import org.baeldung.persistence.model.User;
import org.baeldung.persistence.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PersistenceJPAConfig.class }, loader = AnnotationConfigContextLoader.class)
@Transactional
@TransactionConfiguration
public class JPACriteriaQueryTest {

    @Autowired
    private UserService userService;

    private User userJohn;

    private User userTom;

    @Before
    public void init() {
        userJohn = new User();
        userJohn.setFirstName("John");
        userJohn.setLastName("Doe");
        userJohn.setEmail("john@doe.com");
        userJohn.setAge(22);
        userService.saveUser(userJohn);

        userTom = new User();
        userTom.setFirstName("Tom");
        userTom.setLastName("Doe");
        userTom.setEmail("tom@doe.com");
        userTom.setAge(26);
        userService.saveUser(userTom);
    }

    @Test
    public void givenFirstAndLastName_whenGettingListOfUsers_thenCorrect() {
        final List<User> result = userService.searchUser("John", "Doe", 0);

        assertEquals(1, result.size());
        assertEquals(userJohn.getEmail(), result.get(0).getEmail());
    }

    @Test
    public void givenLast_whenGettingListOfUsers_thenCorrect() {
        final List<User> result = userService.searchUser("", "doe", 0);
        assertEquals(2, result.size());
    }

    @Test
    public void givenLastAndAge_whenGettingListOfUsers_thenCorrect() {
        final List<User> result = userService.searchUser("", "doe", 25);

        assertEquals(1, result.size());
        assertEquals(userTom.getEmail(), result.get(0).getEmail());
    }

    @Test
    public void givenWrongFirstAndLast_whenGettingListOfUsers_thenCorrect() {
        final List<User> result = userService.searchUser("Adam", "Fox", 0);
        assertEquals(0, result.size());
    }

    @Test
    public void givenPartialFirst_whenGettingListOfUsers_thenCorrect() {
        final List<User> result = userService.searchUser("jo", "", 0);

        assertEquals(1, result.size());
        assertEquals(userJohn.getEmail(), result.get(0).getEmail());
    }
}
