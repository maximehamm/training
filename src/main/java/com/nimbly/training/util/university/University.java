package com.nimbly.training.util.university;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

@SuppressWarnings("DataFlowIssue")
public class University {

    private String name;
    private String country;
    private String[] domains;
    private String iso2;
    private String stateProvince;
    private String[] webPages;
    private byte[] flag;

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
            this.flag = getClass().getClassLoader()
                    .getResourceAsStream("images/flags/" + iso2.toLowerCase() + ".png")
                    .readAllBytes();
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
	    "alpha_two_code": "TR",
	    "country": "Turkey",
	    "state-province": null,
	    "domains": [
	        "sabanciuniv.edu",
	        "sabanciuniv.edu.tr"
	    ],
	    "name": "Sabanci University",
	    "web_pages": [
	        "http://www.sabanciuniv.edu/",
	        "http://www.sabanciuniv.edu.tr/"
	    ],
	},
     */
