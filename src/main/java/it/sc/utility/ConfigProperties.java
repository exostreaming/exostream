package it.sc.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigProperties {
    private static final Properties properties = new Properties();
    public static String URL = "";
    public static String URL_IMAGE = "";

    static {
    	try (FileInputStream input = new FileInputStream("bin/config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento del file di configurazione", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
}
