package no.unit.nva.npi.subjectheadings;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.unit.nva.npi.subjectheadings.utils.ResourceUtility;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

public class MainHandler implements RequestStreamHandler {

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ALLOWED_ORIGIN_ENV = "ALLOWED_ORIGIN";
    public static final String ENVIRONMENT_VARIABLE_NOT_SET = "Environment variable not set: ";
    public static final String MISSING_LANGUAGE_IN_PATH_PARAMETERS = "Missing language in path parameters";
    public static final String PATH_PARAMETERS_LANGUAGE = "/pathParameters/language";
    public static final String ENGLISH = "en";
    public static final String NORWEGIAN_BOKMAAL = "nb";
    public static final String NORWEGIAN_NYNORSK = "nn";
    public static final List<String> ALLOWED_LANGUAGES = List.of(ENGLISH, NORWEGIAN_BOKMAAL, NORWEGIAN_NYNORSK);
    public static final String UNRECOGNIZED_LANGUAGE_TEMPLATE =
            "The language value %s was not recognized (Allowed: " + String.join(", ", ALLOWED_LANGUAGES) + ")";
    public static final String FILENAME_TEMPLATE = "%s.json";
    private static final String APPLICATION_PROBLEM_JSON = "application/problem+json";

    private final transient ObjectMapper objectMapper;
    private final transient String allowedOrigin;

    public MainHandler() {
        this(createObjectMapper(), new Environment());
    }

    /**
     * Constructor for MainHandler.
     *
     * @param objectMapper objectMapper
     * @param environment  environment
     */
    public MainHandler(ObjectMapper objectMapper,
                       Environment environment) {
        this.objectMapper = objectMapper;
        this.allowedOrigin = environment.get(ALLOWED_ORIGIN_ENV)
                .orElseThrow(() -> new IllegalStateException(ENVIRONMENT_VARIABLE_NOT_SET + ALLOWED_ORIGIN_ENV));
    }


    private Map<String, String> headers() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
        headers.put(CONTENT_TYPE, APPLICATION_JSON.getMimeType());
        return headers;
    }

    private Map<String, String> problemHeaders() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
        headers.put(CONTENT_TYPE, APPLICATION_PROBLEM_JSON);
        return headers;
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String language;
        try {
            JsonNode event = objectMapper.readTree(input);
            language = Optional.ofNullable(event.at(PATH_PARAMETERS_LANGUAGE).textValue())
                    .orElseThrow(() -> new IllegalArgumentException(MISSING_LANGUAGE_IN_PATH_PARAMETERS));
            if (!ALLOWED_LANGUAGES.contains(language)) {
                throw new IllegalArgumentException(String.format(UNRECOGNIZED_LANGUAGE_TEMPLATE, language));
            }
        } catch (IOException | IllegalArgumentException e) {
            log(Arrays.toString(e.getStackTrace()));
            objectMapper.writeValue(output, new GatewayResponse<>(objectMapper.writeValueAsString(
                    Problem.valueOf(BAD_REQUEST, e.getMessage())), problemHeaders(), SC_BAD_REQUEST));
            return;
        }

        log("Request for subject headings language: " + language);
        String subjectHeadings;
        try {
            subjectHeadings = ResourceUtility.stringOf(String.format(FILENAME_TEMPLATE, language));

        } catch (IOException e) {
            log(Arrays.toString(e.getStackTrace()));
            objectMapper.writeValue(output, new GatewayResponse<>(objectMapper.writeValueAsString(
                    Problem.valueOf(INTERNAL_SERVER_ERROR, e.getMessage())), problemHeaders(),
                    SC_INTERNAL_SERVER_ERROR));
            return;
        }

        objectMapper.writeValue(output, new GatewayResponse<>(
                subjectHeadings, headers(), SC_OK));
    }

    /**
     * Create ObjectMapper.
     *
     * @return objectMapper
     */
    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .registerModule(new ProblemModule())
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
