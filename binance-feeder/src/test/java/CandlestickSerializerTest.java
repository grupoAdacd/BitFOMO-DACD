import com.bitfomo.domain.CandlestickData;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import es.ulpgc.dacd.bitfomo.binancefeeder.transformer.CandlestickSerializer;
import static org.junit.jupiter.api.Assertions.*;

public class CandlestickSerializerTest {

    private CandlestickSerializer serializer;
    private CandlestickData sampleCandlestick;
    private final long klineOpenTime = 1620000000000L;
    private final String openPrice = "50000.00";
    private final String highPrice = "51000.00";
    private final String lowPrice = "49500.00";
    private final String closePrice = "50500.00";
    private final String volume = "125.5";
    private final long klineCloseTime = 1620003600000L;
    private final String quoteAssetVolume = "6300000.00";
    private final int numberOfTrades = 1250;
    @BeforeEach
    public void setUp() {
        serializer = new CandlestickSerializer();
        sampleCandlestick = new CandlestickData(
                klineOpenTime,
                openPrice,
                highPrice,
                lowPrice,
                closePrice,
                volume,
                klineCloseTime,
                quoteAssetVolume,
                numberOfTrades
        );
    }
    @Test
    public void testSerialize() {
        JSONObject result = serializer.serialize(sampleCandlestick);
        assertNotNull(result);
        assertEquals(sampleCandlestick.getTs(), result.getString("ts"));
        assertEquals(sampleCandlestick.getSs(), result.getString("ss"));
        assertEquals(sampleCandlestick.getKlineOpenTime(), result.getLong("klineOpenTime"));
        assertEquals(sampleCandlestick.getOpenPrice(), result.getString("openPrice"));
        assertEquals(sampleCandlestick.getHighPrice(), result.getString("highPrice"));
        assertEquals(sampleCandlestick.getLowPrice(), result.getString("lowPrice"));
        assertEquals(sampleCandlestick.getClosePrice(), result.getString("closePrice"));
        assertEquals(sampleCandlestick.getQuoteAssetVolume(), result.getString("quoteAssetVolume"));
        assertEquals(sampleCandlestick.getVolume(), result.getString("volume"));
        assertEquals(sampleCandlestick.getKlineCloseTime(), result.getLong("klineCloseTime"));
        assertEquals(sampleCandlestick.getNumberOfTrades(), result.getInt("numberOfTrades"));
    }
    @Test
    public void testSerializeWithNullCandlestick() {
        assertThrows(NullPointerException.class, () -> {
            serializer.serialize(null);
        });
    }
    @Test
    public void testSerializeToString() {
        JSONObject result = serializer.serialize(sampleCandlestick);
        String jsonString = result.toString();
        assertFalse(jsonString.isEmpty());
        JSONObject recreatedObject = new JSONObject(jsonString);
        assertEquals(sampleCandlestick.getOpenPrice(), recreatedObject.getString("openPrice"));
        assertEquals(sampleCandlestick.getHighPrice(), recreatedObject.getString("highPrice"));
    }
    @Test
    public void testSerializeMultipleInstances() {
        CandlestickData secondCandlestick = new CandlestickData(
                klineOpenTime + 3600000,
                "51000.00",
                "52000.00",
                "50800.00",
                "51800.00",
                "130.5",
                klineCloseTime + 3600000,
                "6700000.00",
                1300
        );
        JSONObject firstResult = serializer.serialize(sampleCandlestick);
        JSONObject secondResult = serializer.serialize(secondCandlestick);
        assertNotEquals(firstResult.toString(), secondResult.toString());
        assertEquals(sampleCandlestick.getOpenPrice(), firstResult.getString("openPrice"));
        assertEquals(secondCandlestick.getOpenPrice(), secondResult.getString("openPrice"));
    }
}