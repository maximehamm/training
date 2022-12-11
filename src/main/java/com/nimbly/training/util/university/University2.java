package com.nimbly.training.util.university;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class University2 {

    private static final Map<String, byte[]> flagCache = new HashMap<>();

    private String name;
    private String country;
    private String[] domains;
    private String iso2;
    private String stateProvince;
    private String[] webPages;
    private byte[] flag;

    public University2() {
    }

    public University2(String name, String iso2) {
        setName(name);
        setIso2(iso2);
    }

    public static void cleanCache() {
        flagCache.clear();
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String[] getDomains() {
        return domains;
    }

    public String getIso2() {
        return iso2;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public String[] getWebPages() {
        return webPages;
    }

    public byte[] getFlag() {
        return flag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDomains(String[] domains) {
        this.domains = domains;
    }

    @JsonProperty("alpha_two_code")
    public void setIso2(String iso2) {
        this.iso2 = iso2;
        try {
            this.flag = flagCache.get(this.iso2);
            if (this.flag == null) {
                this.flag = getClass().getClassLoader().getResourceAsStream("images/flags/" + iso2.toLowerCase() + ".png").readAllBytes();
                flagCache.put(this.iso2, this.flag);
            }
        } catch (IOException e) {
            throw new RuntimeException("Flag not found", e);
        }
    }

    @JsonProperty("state-province")
    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    @JsonProperty("web_pages")
    public void setWebPages(String[] webPages) {
        this.webPages = webPages;
    }
}

    /*
      {
        "web_pages": [
          "http://www.lpu.in/"
        ],
        "name": "Lovely Professional University",
        "alpha_two_code": "IN",
        "state-province": "Punjab",
        "domains": [
          "lpu.in"
        ],
        "country": "India"
      },
     */
