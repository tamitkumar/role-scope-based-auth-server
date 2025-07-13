package com.tech.brain.config;

import com.tech.brain.exception.AuthException;
import com.tech.brain.exception.ErrorCode;
import com.tech.brain.exception.ErrorSeverity;
import com.tech.brain.utils.AuthConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@Configuration
public class AuthDBConfig {
    @Bean
    DataSource dataSource() {
        log.info("In AuthDBConfig");
        String dbUser = AuthConstant.DB_USER_NAME;
        String dbPassword = AuthConstant.DB_PASSWORD;
        String driverClassName = AuthConstant.DB_DRIVER_CLASS_NAME;
        DriverManagerDataSource ds = new DriverManagerDataSource(getDBUrl(), dbUser, dbPassword);
        try {
            log.info("In AuthDBConfig ===> Set Driver Class");
            ds.setDriverClassName(driverClassName);
        } catch (Exception e) {
            log.error("In AuthDBConfig ===> caught exception {}", e.getMessage());
            throw new AuthException(ErrorCode.ERR002.getErrorCode(), ErrorSeverity.FATAL,
                    ErrorCode.ERR002.getErrorMessage(), e);
        }
        try {
            log.info("In AuthDBConfig ==> close DB Connection");
            ds.getConnection().close();
        } catch (SQLException e) {
            log.error("In AuthDBConfig ===> caught exception {} while closing connection", e.getMessage());
            throw new AuthException(ErrorCode.ERR002.getErrorCode(), ErrorSeverity.FATAL,
                    ErrorCode.ERR002.getErrorMessage(), e);
        }
        return ds;
    }

    private String getDBUrl() {
        log.info("In AuthDBConfig ===> getDBUrl");
        String dbHost = AuthConstant.DB_HOST;
        String dbPort = AuthConstant.DB_PORT;
        String dbName = AuthConstant.DB_NAME;
        String dbUrlPrefix = AuthConstant.DB_URL_PREFIX;
        //		baseUrl.append(EMPConstant.COLON);
        return dbUrlPrefix + dbHost +
                AuthConstant.COLON +
                dbPort +
//		baseUrl.append(EMPConstant.COLON);
                dbName;
    }
}
