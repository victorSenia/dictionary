package org.leo.dictionary.config;

import org.junit.jupiter.api.Test;
import org.leo.dictionary.config.entity.ParseWords;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigParserTest {

    @Test
    void createConfigAppliesProvidedMapValues() {
        ParseWords parseWords = new ParseWords();
        parseWords.setProperties(new HashMap<>());

        String prefix = ParseWords.class.getCanonicalName() + ".";
        Map<String, String> properties = new HashMap<>();
        properties.put(prefix + "languageFrom", "de");
        properties.put(prefix + "languagesTo", "en;es");
        properties.put(prefix + "articles", "der ;die ;das ");
        properties.put(prefix + "delimiter", "\\|");
        properties.put(prefix + "additionalInformationDelimiter", "#");
        properties.put(prefix + "translationDelimiter", ",");
        properties.put(prefix + "topicFlag", "\t");
        properties.put(prefix + "topicDelimiter", "");
        properties.put(prefix + "path", "C:\\words.txt");
        properties.put(prefix + "rootTopic", "A1");

        ConfigParser.createConfig(properties, parseWords);

        assertEquals("de", parseWords.getLanguageFrom());
        assertEquals(List.of("en", "es"), parseWords.getLanguagesTo());
        assertEquals(List.of("der ", "die ", "das "), parseWords.getArticles());
        assertEquals("\\|", parseWords.getDelimiter());
        assertEquals("#", parseWords.getAdditionalInformationDelimiter());
        assertEquals(",", parseWords.getTranslationDelimiter());
        assertEquals("\t", parseWords.getTopicFlag());
        assertEquals("", parseWords.getTopicDelimiter());
        assertEquals("C:\\words.txt", parseWords.getPath());
        assertEquals("A1", parseWords.getRootTopic());
    }

    @Test
    void writeConfigInPropertiesWritesCurrentStateValues() {
        ParseWords parseWords = new ParseWords();
        parseWords.setProperties(new HashMap<>());
        parseWords.setLanguageFrom("fr");
        parseWords.setLanguagesTo(List.of("en", "de"));
        parseWords.setArticles(List.of("le ", "la "));
        parseWords.setDelimiter("\\|");
        parseWords.setAdditionalInformationDelimiter(";");
        parseWords.setTranslationDelimiter(",");
        parseWords.setTopicFlag("\t");
        parseWords.setTopicDelimiter("");
        parseWords.setPath("/tmp/words.txt");
        parseWords.setRootTopic("B1");

        Map<Object, Object> properties = new HashMap<>();
        ConfigParser.writeConfigInProperties(properties, parseWords);

        String prefix = ParseWords.class.getCanonicalName() + ".";
        assertEquals("fr", properties.get(prefix + "languageFrom"));
        assertEquals("en;de", properties.get(prefix + "languagesTo"));
        assertEquals("le ;la ", properties.get(prefix + "articles"));
        assertEquals("\\|", properties.get(prefix + "delimiter"));
        assertEquals(";", properties.get(prefix + "additionalInformationDelimiter"));
        assertEquals(",", properties.get(prefix + "translationDelimiter"));
        assertEquals("\t", properties.get(prefix + "topicFlag"));
        assertEquals("", properties.get(prefix + "topicDelimiter"));
        assertEquals("/tmp/words.txt", properties.get(prefix + "path"));
        assertEquals("B1", properties.get(prefix + "rootTopic"));

        String listValue = String.valueOf(properties.get(prefix + "languagesTo"));
        assertEquals(List.of("en", "de"), Arrays.asList(listValue.split(";")));
    }

    @Test
    void createConfigThrowsForInvalidNumericValues() {
        NumericConfig config = new NumericConfig();
        String prefix = NumericConfig.class.getCanonicalName() + ".";
        Map<String, String> properties = new HashMap<>();
        properties.put(prefix + "count", "NaN");

        assertThrows(NumberFormatException.class, () -> ConfigParser.createConfig(properties, config));
    }

    public static class NumericConfig {
        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
