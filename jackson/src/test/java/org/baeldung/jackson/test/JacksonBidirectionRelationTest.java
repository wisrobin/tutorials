package org.baeldung.jackson.test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.baeldung.jackson.bidirection.Item;
import org.baeldung.jackson.bidirection.ItemWithIdentity;
import org.baeldung.jackson.bidirection.ItemWithIgnore;
import org.baeldung.jackson.bidirection.ItemWithRef;
import org.baeldung.jackson.bidirection.ItemWithSerializer;
import org.baeldung.jackson.bidirection.User;
import org.baeldung.jackson.bidirection.UserWithIdentity;
import org.baeldung.jackson.bidirection.UserWithIgnore;
import org.baeldung.jackson.bidirection.UserWithRef;
import org.baeldung.jackson.bidirection.UserWithSerializer;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonBidirectionRelationTest {

    @Test(expected = JsonMappingException.class)
    public void givenBidirectionRelation_whenSerializing_thenException() throws JsonProcessingException {
        final User user = new User(1, "John");
        final Item item = new Item(2, "book", user);
        user.addItem(item);

        new ObjectMapper().writeValueAsString(item);
    }

    @Test
    public void givenBidirectionRelation_whenUsingJacksonReferenceAnnotation_thenCorrect() throws JsonProcessingException {
        final UserWithRef user = new UserWithRef(1, "John");
        final ItemWithRef item = new ItemWithRef(2, "book", user);
        user.addItem(item);

        final String result = new ObjectMapper().writeValueAsString(item);

        assertThat(result, containsString("book"));
        assertThat(result, containsString("John"));
        assertThat(result, not(containsString("userItems")));
    }

    @Test
    public void givenBidirectionRelation_whenUsingJsonIdentityInfo_thenCorrect() throws JsonProcessingException {
        final UserWithIdentity user = new UserWithIdentity(1, "John");
        final ItemWithIdentity item = new ItemWithIdentity(2, "book", user);
        user.addItem(item);

        final String result = new ObjectMapper().writeValueAsString(item);

        assertThat(result, containsString("book"));
        assertThat(result, containsString("John"));
        assertThat(result, containsString("userItems"));
    }

    @Test
    public void givenBidirectionRelation_whenUsingJsonIgnore_thenCorrect() throws JsonProcessingException {
        final UserWithIgnore user = new UserWithIgnore(1, "John");
        final ItemWithIgnore item = new ItemWithIgnore(2, "book", user);
        user.addItem(item);

        final String result = new ObjectMapper().writeValueAsString(item);

        assertThat(result, containsString("book"));
        assertThat(result, containsString("John"));
        assertThat(result, not(containsString("userItems")));
    }

    @Test
    public void givenBidirectionRelation_whenUsingCustomSerializer_thenCorrect() throws JsonProcessingException {
        final UserWithSerializer user = new UserWithSerializer(1, "John");
        final ItemWithSerializer item = new ItemWithSerializer(2, "book", user);
        user.addItem(item);

        final String result = new ObjectMapper().writeValueAsString(item);

        assertThat(result, containsString("book"));
        assertThat(result, containsString("John"));
        assertThat(result, containsString("userItems"));
    }

    @Test
    public void givenBidirectionRelation_whenDeserializingUsingIdentity_thenCorrect() throws JsonProcessingException, IOException {
        final String json = "{\"id\":2,\"itemName\":\"book\",\"owner\":{\"id\":1,\"name\":\"John\",\"userItems\":[2]}}";

        final ItemWithIdentity item = new ObjectMapper().reader(ItemWithIdentity.class).readValue(json);

        assertEquals(2, item.id);
        assertEquals("book", item.itemName);
        assertEquals("John", item.owner.name);
    }

    @Test
    public void givenBidirectionRelation_whenUsingCustomDeserializer_thenCorrect() throws JsonProcessingException, IOException {
        final String json = "{\"id\":2,\"itemName\":\"book\",\"owner\":{\"id\":1,\"name\":\"John\",\"userItems\":[2]}}";

        final ItemWithSerializer item = new ObjectMapper().reader(ItemWithSerializer.class).readValue(json);
        assertEquals(2, item.id);
        assertEquals("book", item.itemName);
        assertEquals("John", item.owner.name);
    }

}
