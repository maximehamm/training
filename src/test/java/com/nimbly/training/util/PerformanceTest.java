package com.nimbly.training.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"DataFlowIssue", "Java8MapApi"})
public class PerformanceTest {

    @Test
    public void test1() throws IOException {

        // Load universities
        byte[] jsonData = getClass().getClassLoader().getResourceAsStream("universities_worlwide.json").readAllBytes();
        List<University> universities = new ObjectMapper().readValue(jsonData, new TypeReference<>() {
        });

        // Build map
        int i = 1;
        Map<String, List<University>> countriesMap = new HashMap<>();
        for (University university : universities) {

            System.out.println((i++) + " " + university.name);

            // Add to coutry map
            List<University> countryUniversities = countriesMap.computeIfAbsent(university.country, k -> new ArrayList<>());
            countryUniversities.add(university);

            // Add flag
            university.flag = getClass().getClassLoader().getResourceAsStream("images/flags/" + university.iso2.toLowerCase() + ".png").readAllBytes();
        }

        Assertions.assertEquals(9810, universities.size());
        Assertions.assertEquals(204, countriesMap.size());

        //
        // ANALYSING PERFOMANCES :
        //
        //  - Loading flags takes a while !
        //  --> Flags are loaded 9810 times... there is only 204 country ! let's use a cache for flags !
    }

    @Test
    public void test2() throws IOException {

        // Load universities
        byte[] jsonData = getClass().getClassLoader().getResourceAsStream("universities_worlwide.json").readAllBytes();
        List<University> universities = new ObjectMapper().readValue(jsonData, new TypeReference<>() {
        });

        // Build map
        int i = 1;
        Map<String, byte[]> flagCache = new HashMap<>();
        Map<String, List<University>> countriesMap = new HashMap<>();
        for (University university : universities) {

            System.out.println((i++) + " " + university.name);

            // Add to coutry map
            List<University> countryUniversities = countriesMap.computeIfAbsent(university.country, k -> new ArrayList<>());
            countryUniversities.add(university);

            // Add flag
            university.flag = flagCache.get(university.iso2.toLowerCase());
            if (university.flag == null) {
                university.flag = getClass().getClassLoader().getResourceAsStream("images/flags/" + university.iso2.toLowerCase() + ".png").readAllBytes();
                flagCache.put(university.iso2.toLowerCase(), university.flag);
            }
        }

        Assertions.assertEquals(9810, universities.size());
        Assertions.assertEquals(204, countriesMap.size());

        //
        // ANALYSING PERFOMANCES :
        //
        //  - Loading Json takes a while !
        //  --> Let's use a simple custom parsing...
    }

    @Test
    public void test3() throws IOException {

        // Load universities
        List<University> universities = new ArrayList<>();
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("universities_worlwide.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        University un = null;
        String line = reader.readLine();
        while (line != null) {
            String trim = line.trim();
            if (trim.startsWith("\"name")) {
                un = new University();
                un.name = trim.substring(9, trim.length() - 2);
                universities.add(un);
            }
            else if (trim.startsWith("\"alpha_two_code")) {
                un.iso2 = trim.substring(19, trim.length() - 2);
            }
            line = reader.readLine();
        }
        reader.close();

        // Build map
        int i = 1;
        Map<String, byte[]> flagCache = new HashMap<>();
        Map<String, List<University>> countriesMap = new HashMap<>();
        for (University university : universities) {

            System.out.println((i++) + " " + university.name);

            // Add to coutry map
            List<University> countryUniversities = countriesMap.computeIfAbsent(university.iso2, k -> new ArrayList<>());
            countryUniversities.add(university);

            // Add flag
            university.flag = flagCache.get(university.iso2.toLowerCase());
            if (university.flag == null) {
                university.flag = getClass().getClassLoader().getResourceAsStream("images/flags/" + university.iso2.toLowerCase() + ".png").readAllBytes();
                flagCache.put(university.iso2.toLowerCase(), university.flag);
            }
        }

        Assertions.assertEquals(9810, universities.size());
        Assertions.assertEquals(204, countriesMap.size());

        //
        // ANALYSING PERFOMANCES :
        //
        //  - While loading flags ?
        //  - While building "universities" list and "countriesMap" map ? We only need to count them !
        //  - Do we really need to use instancies of "University" ??
        //  --> Let's use only counters and removing use of "University" class
    }

    @Test
    public void test4() throws IOException {

        int i = 0;
        Map<String, Integer> countriesMap = new HashMap<>();

        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("universities_worlwide.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String universityName;
        String line = reader.readLine();
        while (line != null) {

            String trim = line.trim();
            if (trim.startsWith("\"name")) {

                universityName = trim.substring(9, trim.length() - 2);
                System.out.println((++i) + " " + universityName);
            }
            else if (trim.startsWith("\"alpha_two_code")) {
                String iso2 = trim.substring(19, trim.length() - 2);

                Integer count = countriesMap.get(iso2);
                if (count == null) {
                    countriesMap.put(iso2, 1);
                }
                else {
                    countriesMap.put(iso2, count + 1);
                }
            }
            line = reader.readLine();
        }
        reader.close();

        Assertions.assertEquals(9810, i);
        Assertions.assertEquals(204, countriesMap.size());

        //
        // ANALYSING PERFOMANCES :
        //
        //  - The most consuming method is now "println" !
        //  --> Let's remove it !
    }

    @Test
    public void test5() throws IOException {

        int i = 0;
        Map<String, Integer> countriesMap = new HashMap<>();

        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("universities_worlwide.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line = reader.readLine();
        while (line != null) {

            String trim = line.trim();
            if (trim.startsWith("\"alpha_two_code")) {
                String iso2 = trim.substring(19, trim.length() - 2);

                ++i;
                Integer count = countriesMap.get(iso2);
                if (count == null) {
                    countriesMap.put(iso2, 1);
                }
                else {
                    countriesMap.put(iso2, count + 1);
                }
            }
            line = reader.readLine();
        }
        reader.close();

        Assertions.assertEquals(9810, i);
        Assertions.assertEquals(204, countriesMap.size());

        //
        // ANALYSING PERFOMANCES :
        //
        //  - The most consuming method is now "readLine"... we can't do anything here
        //  - The second most consuming method is "trim"... it cost only 10% of time... and 50% of memory
        //  --> Let's remove the trim method
        //
        // - The buffer reader uses 8Kbytes...
        //  --> Let's try to reduce buffer size, using org.apache.commons.io api...
    }

    @SuppressWarnings("resource")
    @Test
    public void test6() throws IOException, URISyntaxException {

        int i = 0;
        Map<String, Integer> countriesMap = new HashMap<>();

        URL url = getClass().getClassLoader().getResource("universities_worlwide.json");
        LineIterator lineIterator = FileUtils.lineIterator(new File(url.toURI()));
        while (lineIterator.hasNext()) {
            String line = lineIterator.nextLine();

            if (line.startsWith("    \"alpha_two_code")) {
                String iso2 = line.substring(23, line.length() - 2);

                ++i;
                Integer count = countriesMap.get(iso2);
                if (count == null) {
                    countriesMap.put(iso2, 1);
                }
                else {
                    countriesMap.put(iso2, count + 1);
                }
            }
        }

        Assertions.assertEquals(9810, i);
        Assertions.assertEquals(204, countriesMap.size());

        //
        // ANALYSING PERFOMANCES :
        //
        //  - A few better regarding memory... same time line
        //  --> Let's say the method is optimized enough !
    }


    @SuppressWarnings("unused")
    static class University {
        public String name;
        public String country;
        public String[] domains;

        @JsonProperty("alpha_two_code")
        public String iso2;
        @JsonProperty("state-province")
        public String stateProvince;
        @JsonProperty("web_pages")
        public String[] webPages;

        public byte[] flag;
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
}
