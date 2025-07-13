package com.tech.brain.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.MySQLIdentityColumnSupport;

@Slf4j
public class DialectConfig extends MySQLDialect {
    @Override
    public boolean dropConstraints() {
        log.info("DialectConfig ===> Prevents Hibernate from trying to remove foreign keys when dropping tables. Safe for existing DBs.");
        return false;
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        log.info("DialectConfig ===> telling Hibernate how to fetch the auto-generated ID after an INSERT");
        return new MySQLIdentityColumnSupport() {
            @Override
            public String getIdentitySelectString(String table, String column, int type) {
                log.info("DialectConfig ===> table {}, column {},type {}", table, column, type);
                return "select last_insert_id()";
            }
        };
    }
}
