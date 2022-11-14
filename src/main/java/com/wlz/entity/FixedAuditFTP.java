package com.wlz.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fixedauditftp")
public class FixedAuditFTP {

    private  String host;

    private  int port;

    private  String username;

    private  String password;

    private String fixedPath;

    public  String getHost() {
        return host;
    }

    public  int getPort() {
        return port;
    }

    public  String getUsername() {
        return username;
    }

    public  String getPassword() {
        return password;
    }

    public String getFixedPath() {
        return fixedPath;
    }

    public void setFixedPath(String fixedPath) {
        this.fixedPath = fixedPath;
    }

    public  void setHost(String host) {
        this.host = host;
    }

    public  void setPort(int port) {
        this.port = port;
    }

    public  void setUsername(String username) {
        this.username = username;
    }

    public  void setPassword(String password) {
        this.password = password;
    }

}
