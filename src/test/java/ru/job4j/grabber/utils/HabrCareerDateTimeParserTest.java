package ru.job4j.grabber.utils;

import org.testng.annotations.Test;

import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

public class HabrCareerDateTimeParserTest {

    @Test
    public void whenParseDate1() {
        HabrCareerDateTimeParser hcdt = new HabrCareerDateTimeParser();
        String in = "2023-01-17T22:10:10+03:00";
        assertThat(hcdt.parse(in)).isEqualTo("2023-01-17T22:10:10");
    }
}