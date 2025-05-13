package com.bitfomo.adapters;

import com.bitfomo.domain.CandlestickData;

import java.sql.Connection;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CandlestickDBPersistence {
    private long lastKlineIntroduced;

    public long getLastKlineIntroduced() {
        return lastKlineIntroduced;
    }

    public void setLastKlineIntroduced(long lastKlineIntroduced) {
        this.lastKlineIntroduced = lastKlineIntroduced;
    }
}
