
import com.bitfomo.transformer.JsonDataParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class JsonDataParserTest {
    private static final String VALID_JSON_OBJECT = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
    private static final String VALID_NESTED_JSON_OBJECT = "{\"person\":{\"name\":\"John\",\"age\":30},\"isActive\":true}";
    private static final String VALID_JSON_ARRAY = "[1,2,3,4,5]";
    private static final String VALID_OBJECT_ARRAY = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]";
    private static final String INVALID_JSON = "{name:John}";
    private static final String INVALID_ARRAY = "[1,2,3,";
    private JsonDataParser validObjectParser;
    private JsonDataParser validNestedObjectParser;
    private JsonDataParser validArrayParser;
    private JsonDataParser validObjectArrayParser;
    private JsonDataParser invalidJsonParser;
    private JsonDataParser invalidArrayParser;

    @BeforeEach
    public void setUp() {
        validObjectParser = new JsonDataParser(VALID_JSON_OBJECT);
        validNestedObjectParser = new JsonDataParser(VALID_NESTED_JSON_OBJECT);
        validArrayParser = new JsonDataParser(VALID_JSON_ARRAY);
        validObjectArrayParser = new JsonDataParser(VALID_OBJECT_ARRAY);
        invalidJsonParser = new JsonDataParser(INVALID_JSON);
        invalidArrayParser = new JsonDataParser(INVALID_ARRAY);
    }

    @Test
    public void testParseValidJsonObject() {
        try {
            JSONObject result = validObjectParser.parseObject();
            assertNotNull(result);
            assertEquals("John", result.getString("name"));
            assertEquals(30, result.getInt("age"));
            assertEquals("New York", result.getString("city"));
        } catch (JSONException e) {
            fail("It shouldn't return an error given a valid JSON object: " + e.getMessage());
        }
    }
    @Test
    public void testParseNestedJsonObject() {
        try {
            JSONObject result = validNestedObjectParser.parseObject();
            assertNotNull(result);
            assertTrue(result.has("person"));
            assertTrue(result.get("person") instanceof JSONObject);
            JSONObject person = result.getJSONObject("person");
            assertEquals("John", person.getString("name"));
            assertEquals(30, person.getInt("age"));
            assertTrue(result.getBoolean("isActive"));
        } catch (JSONException e) {
            fail("It shouldn't return an error given a valid nested JSON object: " + e.getMessage());
        }
    }

    @Test
    public void testParseValidJsonArray() {
        try {
            JSONArray result = validArrayParser.parseArray();
            assertNotNull(result);
            assertEquals(5, result.length());
            assertEquals(1, result.getInt(0));
            assertEquals(2, result.getInt(1));
            assertEquals(3, result.getInt(2));
            assertEquals(4, result.getInt(3));
            assertEquals(5, result.getInt(4));
        } catch (JSONException e) {
            fail("It shouldn't return an error given a valid json array: " + e.getMessage());
        }
    }

    @Test
    public void testParseValidObjectArray() {
        try {
            JSONArray result = validObjectArrayParser.parseArray();
            assertNotNull(result);
            assertEquals(2, result.length());
            JSONObject firstPerson = result.getJSONObject(0);
            JSONObject secondPerson = result.getJSONObject(1);
            assertEquals("John", firstPerson.getString("name"));
            assertEquals("Jane", secondPerson.getString("name"));
        } catch (JSONException e) {
            fail("It shouldn't have to throw any exception given a valid object's array: " + e.getMessage());
        }
    }
    @Test
    public void testParseInvalidJsonArray() {
        assertThrows(JSONException.class, () -> {
            invalidArrayParser.parseArray();
        });
    }
    @Test
    public void testParseArrayAsObject() {
        assertThrows(JSONException.class, () -> {
            validArrayParser.parseObject();
        });
    }
    @Test
    public void testParseObjectAsArray() {
        assertThrows(JSONException.class, () -> {
            validObjectParser.parseArray();
        });
    }
    @Test
    public void testParseEmptyString() {
        JsonDataParser emptyParser = new JsonDataParser("");
        assertThrows(JSONException.class, () -> {
            emptyParser.parseObject();
        });
        assertThrows(JSONException.class, () -> {
            emptyParser.parseArray();
        });
    }
}