package com.myself.sEverything.config;

import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Data
public class ConfigProperties {
    private String username;
    private String password;
    private String url;
    private String driverClass;

    public ConfigProperties() {
        //从外部文件加载
        InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties");
        Properties p = new Properties();
        try {
            p.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username = String.valueOf(p.get("username"));
        this.password = String.valueOf(p.get("password"));
        this.driverClass = String.valueOf(p.get("driverClass"));
        this.url = String.valueOf(p.get("url"));

    }

}
