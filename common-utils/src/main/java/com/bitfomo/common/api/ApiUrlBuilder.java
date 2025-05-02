package com.bitfomo.common.api;

public class ApiUrlBuilder implements DefaultApiUrlBuilder{
    public final String baseUrl;
    public final String specify;

    public ApiUrlBuilder(String baseUrl, String specify) {
        this.baseUrl = baseUrl;
        this.specify = specify;
    }
    @Override
    public String createApiUrl() {
        return this.baseUrl + this.specify;
    }
}
