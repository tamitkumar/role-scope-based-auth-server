package com.tech.brain.utils;

public class AuthConstant {
    public static final String COLON = ":";
    public static final String HYPHEN = "-";
    public static final String DB_HOST = "localhost";
    public static final String DB_PORT = "3306/";
    public static final String DB_NAME = "auth?serverTimezone=UTC";
    public static final String DB_URL_PREFIX = "jdbc:mysql://";
    public static final String DB_USER_NAME = "root";
    public static final String DB_PASSWORD = "0130Ec071007";
    public static final String DB_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String DIALECT_KEY = "spring.jpa.properties.hibernate.dialect";
    public static final String DIALECT_VALUE = "org.hibernate.dialect.MySQL8Dialect";
    public static final String SHOW_SQL_KEY = "spring.jpa.show-sql";
    public static final boolean SHOW_SQL_VALUE = true;
    public static final String FORMAT_SQL_KEY = "spring.jpa.properties.hibernate.format_sql";
    public static final boolean FORMAT_SQL_VALUE = true;
    public static final String HTTP_CODE_500 = "500";
    public static final String HTTP_MSG_500 = "Error when processing the request.";
}
