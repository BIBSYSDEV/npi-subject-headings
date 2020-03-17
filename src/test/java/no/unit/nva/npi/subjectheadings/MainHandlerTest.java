package no.unit.nva.npi.subjectheadings;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.nva.npi.subjectheadings.utils.ResourceUtility;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static java.util.Collections.singletonMap;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MainHandlerTest {
    public static final String ALLOWED_ORIGIN_ENV = "ALLOWED_ORIGIN";
    public static final String API_HOST_ENV = "API_HOST";
    public static final String API_SCHEME_ENV = "API_SCHEME";
    public static final String NON_EXISTING_LANGUAGE = "ob";
    public static final String APPLICATION_PROBLEM_JSON = "application/problem+json";
    public static final String ENGLISH_JSON = "en.json";
    public static final String LANGUAGE_CODE_ENGLISH = "en";
    public static final String HTTP = "http";
    public static final String LOCALHOST_3000 = "localhost:3000";
    public static final String ASTERISK = "*";
    public static final String NYNORSK_JSON = "nn.json";
    public static final String LANGUAGE_CODE_NYNORSK = "nn";
    public static final String BOKMAAL_JSON = "nb.json";
    public static final String LANGUAGE_CODE_BOKMAAL = "nb";

    private ObjectMapper objectMapper = MainHandler.createObjectMapper();

    @DisplayName("Test the default constructor")
    @Test
    void testDefaultConstructor() throws Exception {
        withEnvironmentVariable(ALLOWED_ORIGIN_ENV, ASTERISK)
                .and(API_HOST_ENV, LOCALHOST_3000)
                .and(API_SCHEME_ENV, HTTP)
                .execute(() -> {
                    MainHandler mainHandler = new MainHandler();
                    assertNotNull(mainHandler);
                });
    }

    @DisplayName("The handler returns status code 200 and body for request for Norsk Bokmål")
    @Test
    public void testOkResponseNorwegianBokmaal() throws Exception {
        withEnvironmentVariable(ALLOWED_ORIGIN_ENV, ASTERISK)
                .and(API_HOST_ENV, LOCALHOST_3000)
                .and(API_SCHEME_ENV, HTTP)
                .execute(() -> {
                    Environment environment = new Environment();
                    Context context = getMockContext();
                    MainHandler mainHandler = new MainHandler(objectMapper, environment);
                    OutputStream output = new ByteArrayOutputStream();
                    mainHandler.handleRequest(inputStream(LANGUAGE_CODE_BOKMAAL), output, context);
                    GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
                    assertEquals(SC_OK, gatewayResponse.getStatusCode());
                    assertTrue(gatewayResponse.getHeaders().containsKey(CONTENT_TYPE));
                    assertTrue(gatewayResponse.getHeaders().containsKey(MainHandler.ACCESS_CONTROL_ALLOW_ORIGIN));
                    String body = (String) gatewayResponse.getBody();
                    String expected = ResourceUtility.stringOf(BOKMAAL_JSON);
                    assertEquals(expected, body);
                });
    }

    @DisplayName("The handler returns status code 200 and body for request for Norsk Nynorsk")
    @Test
    public void testOkResponseNorwegianNynorsk() throws Exception {
        withEnvironmentVariable(ALLOWED_ORIGIN_ENV, ASTERISK)
                .and(API_HOST_ENV, LOCALHOST_3000)
                .and(API_SCHEME_ENV, HTTP)
                .execute(() -> {
                    Environment environment = new Environment();
                    Context context = getMockContext();
                    MainHandler mainHandler = new MainHandler(objectMapper, environment);
                    OutputStream output = new ByteArrayOutputStream();

                    mainHandler.handleRequest(inputStream(LANGUAGE_CODE_NYNORSK), output, context);

                    GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
                    assertEquals(SC_OK, gatewayResponse.getStatusCode());
                    assertTrue(gatewayResponse.getHeaders().containsKey(CONTENT_TYPE));
                    assertTrue(gatewayResponse.getHeaders().containsKey(MainHandler.ACCESS_CONTROL_ALLOW_ORIGIN));
                    String body = (String) gatewayResponse.getBody();
                    String expected = ResourceUtility.stringOf(NYNORSK_JSON);
                    assertEquals(expected, body);
                });
    }

    @DisplayName("The handler returns status code 200 and body for request for English")
    @Test
    public void testOkResponseEnglish() throws Exception {
        withEnvironmentVariable(ALLOWED_ORIGIN_ENV, ASTERISK)
                .and(API_HOST_ENV, LOCALHOST_3000)
                .and(API_SCHEME_ENV, HTTP)
                .execute(() -> {
                    Environment environment = new Environment();
                    Context context = getMockContext();
                    MainHandler mainHandler = new MainHandler(objectMapper, environment);
                    OutputStream output = new ByteArrayOutputStream();

                    mainHandler.handleRequest(inputStream(LANGUAGE_CODE_ENGLISH), output, context);

                    GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
                    assertEquals(SC_OK, gatewayResponse.getStatusCode());
                    assertTrue(gatewayResponse.getHeaders().containsKey(CONTENT_TYPE));
                    assertTrue(gatewayResponse.getHeaders().containsKey(MainHandler.ACCESS_CONTROL_ALLOW_ORIGIN));
                    String body = (String) gatewayResponse.getBody();
                    String expected = ResourceUtility.stringOf(ENGLISH_JSON);
                    assertEquals(expected, body);
                });
    }


    @DisplayName("The handler returns status code 200 and body for request for non-existent language")
    @Test
    public void testBadRequestResponseForNonExistingLanguage() throws Exception {
        withEnvironmentVariable(ALLOWED_ORIGIN_ENV, ASTERISK)
                .and(API_HOST_ENV, LOCALHOST_3000)
                .and(API_SCHEME_ENV, HTTP)
                .execute(() -> {
                    Environment environment = new Environment();
                    Context context = getMockContext();
                    MainHandler mainHandler = new MainHandler(objectMapper, environment);
                    OutputStream output = new ByteArrayOutputStream();

                    mainHandler.handleRequest(inputStream(NON_EXISTING_LANGUAGE), output, context);

                    GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
                    assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
                    assertTrue(gatewayResponse.getHeaders().containsKey(CONTENT_TYPE));
                    assertEquals(APPLICATION_PROBLEM_JSON, gatewayResponse.getHeaders().get(CONTENT_TYPE));
                    assertTrue(gatewayResponse.getHeaders().containsKey(MainHandler.ACCESS_CONTROL_ALLOW_ORIGIN));
                    String body = (String) gatewayResponse.getBody();
                    String expected = "{\n"
                            + "  \"title\" : \"Bad Request\",\n"
                            + "  \"status\" : 400,\n"
                            + "  \"detail\" : \"The language value " + NON_EXISTING_LANGUAGE + " was not recognized "
                            + "(Allowed: en, nb, nn)\"\n"
                            + "}";
                    assertEquals(expected, body);
                });
    }

    @DisplayName("The handler returns status code 200 and body for request for non-existent language")
    @Test
    public void testBadRequestResponseForMissingLanguage() throws Exception {
        withEnvironmentVariable(ALLOWED_ORIGIN_ENV, ASTERISK)
                .and(API_HOST_ENV, LOCALHOST_3000)
                .and(API_SCHEME_ENV, HTTP)
                .execute(() -> {
                    Environment environment = new Environment();
                    Context context = getMockContext();
                    MainHandler mainHandler = new MainHandler(objectMapper, environment);
                    OutputStream output = new ByteArrayOutputStream();

                    mainHandler.handleRequest(inputStream(null), output, context);

                    GatewayResponse gatewayResponse = objectMapper.readValue(output.toString(), GatewayResponse.class);
                    assertEquals(SC_BAD_REQUEST, gatewayResponse.getStatusCode());
                    assertTrue(gatewayResponse.getHeaders().containsKey(CONTENT_TYPE));
                    assertEquals(APPLICATION_PROBLEM_JSON, gatewayResponse.getHeaders().get(CONTENT_TYPE));
                    assertTrue(gatewayResponse.getHeaders().containsKey(MainHandler.ACCESS_CONTROL_ALLOW_ORIGIN));
                    String body = (String) gatewayResponse.getBody();
                    String expected = "{\n"
                            + "  \"title\" : \"Bad Request\",\n"
                            + "  \"status\" : 400,\n"
                            + "  \"detail\" : \"Missing language in path parameters\"\n"
                            + "}";
                    assertEquals(expected, body);
                });
    }

    private InputStream inputStream(String language) throws IOException {
        Map<String, Object> event = new ConcurrentHashMap<>();
        String body = "";
        event.put("body", body);
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        event.put("headers", headers);
        event.put("pathParameters", singletonMap("language", language));
        return new ByteArrayInputStream(objectMapper.writeValueAsBytes(event));
    }

    private Context getMockContext() {
        return mock(Context.class);
    }
}