package net.hntdstudio.core.model;

import lombok.Getter;
import lombok.Setter;

public class Config {

    @Getter
    @Setter
    private MySQL mysql = new MySQL();

    @Getter
    @Setter
    public static class MySQL {
        private String url = "jdbc:mysql://localhost:3306/";
        private String database = "mydatabase";
        private String username = "root";
        private String password = "password";
    }
}