package de.naivetardis.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesContext {

    private static final PropertiesContext propertiesContext = new PropertiesContext();

    private final Properties properties;

    private PropertiesContext() {
        this.properties = loadProperties();
    }

    public static PropertiesContext getInstance() {
        return propertiesContext;
    }

    private static Properties loadProperties() {
        Properties prop = new Properties();
        try (InputStream input = PropertiesContext.class.getClassLoader().getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException("Not found application.properties");
            }

            //load a properties file from class path, inside static method
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return prop;
    }

    public Properties getContext() {
        return properties;
    }
}
