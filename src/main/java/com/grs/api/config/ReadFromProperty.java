package com.grs.api.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
@Service
public class ReadFromProperty {
    String result = "";
    InputStream inputStream;
    InputStream inputStreamFr;
    static Properties prop;
    static Properties propFr;

    @PostConstruct
    public void init() throws IOException {

        if (prop == null && propFr == null) {
            prop = new Properties();
            propFr = new Properties();
            inputStream = getClass().getClassLoader().getResourceAsStream("messages.properties");
            inputStreamFr = getClass().getClassLoader().getResourceAsStream("messages_fr.properties");

            try {

                if (inputStream != null) {
                    prop.load(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                } else {
                    throw new FileNotFoundException("property file 'messages.properties' not found in the classpath");
                }

                if (inputStreamFr != null) {
                    propFr.load(new InputStreamReader(inputStreamFr, Charset.forName("UTF-8")));
                } else {
                    throw new FileNotFoundException("property file 'messages_fr.properties' not found in the classpath");
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                if (inputStream != null) inputStream.close();
                if (inputStreamFr != null) inputStreamFr.close();
            }
        }
    }

    public String getPropValues(String name, String propFileName) throws IOException {

//        log.info( "Accessing " + propFileName + " for " + name);

        if (propFileName != null) {
            if (propFileName.equals("messages_fr.properties")) return propFr.getProperty(name);
            else return prop.getProperty(name);
        }

        return "";

    }
}
