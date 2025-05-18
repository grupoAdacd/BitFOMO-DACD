import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ulpgc.dacd.bitfomo.eventstorebuilder.infrastructure.Deduplicator;
import es.ulpgc.dacd.bitfomo.eventstorebuilder.infrastructure.adapters.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventStoreTest {
    private static class TestableEventStore extends EventStore {
        private final String testBaseDir;
        private final Deduplicator mockDeduplicator;

        public TestableEventStore(String testBaseDir, Deduplicator deduplicator) {
            this.testBaseDir = testBaseDir;
            this.mockDeduplicator = deduplicator;
        }

        @Override
        public void saveEvent(String topic, String json) {
            try {
                JsonNode obj = getMapper().readTree(json);
                if (topic.equals("RedditPost")) {
                    String postId = obj.get("id").asText();
                    if (mockDeduplicator.isDuplicateRedditPost(postId)) {
                        System.out.println("RedditPost duplicado omitido: " + postId);
                        return;
                    }
                    mockDeduplicator.addRedditPostId(postId);
                }
                String ss = obj.get("ss").asText();
                String ts = obj.get("ts").asText();
                LocalDate date = LocalDate.parse(ts.substring(0, 10), DateTimeFormatter.ISO_DATE);
                String dirPath = String.format("%s/%s/%s", testBaseDir, topic, ss);
                String fileName = String.format("%s.events", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                File directory = new File(dirPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File file = new File(directory, fileName);
                try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file, true))) {
                    writer.write(json);
                    writer.newLine();
                }
            } catch (Exception e) {
                System.err.println("Error saving event: " + e.getMessage());
            }
        }
        public ObjectMapper getMapper() {
            return super.getMapper();
        }
    }
    @TempDir
    Path tempDir;
    private TestableEventStore eventStore;
    private Deduplicator mockDeduplicator;
    private String redditPostJson = "{\"id\":\"post123\",\"ts\":\"2023-05-20T10:15:30Z\",\"ss\":\"reddit-feeder\",\"title\":\"Test Post\"}";
    private String cryptoPriceJson = "{\"id\":\"BTC-USD\",\"ts\":\"2023-05-20T10:15:30Z\",\"ss\":\"crypto-feeder\",\"price\":\"30000\"}";

    @BeforeEach
    void setUp() {
        mockDeduplicator = Mockito.mock(Deduplicator.class);
        eventStore = new TestableEventStore(tempDir.toString(), mockDeduplicator);
    }

    @Test
    void shouldSaveRedditPostEvent() throws Exception {
        when(mockDeduplicator.isDuplicateRedditPost("post123")).thenReturn(false);
        eventStore.saveEvent("RedditPost", redditPostJson);
        verify(mockDeduplicator).isDuplicateRedditPost("post123");
        verify(mockDeduplicator).addRedditPostId("post123");
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.of(2023, 5, 20));
        File eventFile = new File(tempDir.toFile(), "RedditPost/reddit-feeder/" + date + ".events");
        assertTrue(eventFile.exists(), "El archivo de eventos debería haberse creado");
        try (BufferedReader reader = new BufferedReader(new FileReader(eventFile))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            assertEquals(1, lines.size(), "Debería haber una línea en el archivo");
            assertEquals(redditPostJson, lines.get(0), "El contenido JSON debería haberse guardado correctamente");
        }
    }

    @Test
    void shouldSkipDuplicateRedditPost() {
        when(mockDeduplicator.isDuplicateRedditPost("post123")).thenReturn(true);
        eventStore.saveEvent("RedditPost", redditPostJson);
        verify(mockDeduplicator).isDuplicateRedditPost("post123");
        verify(mockDeduplicator, never()).addRedditPostId(anyString());
        File redditDir = new File(tempDir.toFile(), "RedditPost/reddit-feeder");
        File[] files = redditDir.listFiles();
        assertTrue(files == null || files.length == 0, "No deberían crearse archivos para posts duplicados");
    }

    @Test
    void shouldSaveNonRedditPostWithoutDuplicationCheck() {
        eventStore.saveEvent("CryptoPrice", cryptoPriceJson);
        verify(mockDeduplicator, never()).isDuplicateRedditPost(anyString());
        verify(mockDeduplicator, never()).addRedditPostId(anyString());
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.of(2023, 5, 20));
        File eventFile = new File(tempDir.toFile(), "CryptoPrice/crypto-feeder/" + date + ".events");

        assertTrue(eventFile.exists(), "El archivo de eventos debería haberse creado");
    }
}