package es.ulpgc.dacd.bitfomo.businessunit.domain;
import es.ulpgc.dacd.binancefeeder.domain.Candlestick;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BinanceCache implements CacheUtil {
    private Map<String, Candlestick> financialCache;
    private final List<String> keyList = Arrays.asList("ts", "openPrice", "closePrice");

    @Override
    public void insert(){
        BinanceCache binanceCache = new BinanceCache();
        Map<String, Candlestick> result=null;


        binanceCache.setFinancialCache(result);//setter con el diccionario bien
    }

    @Override
    public Candlestick extract(String fileName){// cada line es un candlestick
        return null;
    }

    public Map<String, Candlestick> getFinancialCache() {
        return financialCache;
    }

    public void setFinancialCache(Map<String, Candlestick> financialCache) {
        this.financialCache = financialCache;
    }

    public List<String> getKeyList() {
        return keyList;
    }
}
