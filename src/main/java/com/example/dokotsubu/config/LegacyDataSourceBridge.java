package com.example.dokotsubu.config;

import javax.sql.DataSource;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import util.DBUtil;

/**
 * Makes the Spring-managed DataSource available to the existing DAO layer.
 *
 * <p>This bridge is temporary. It can be removed after the DAOs have migrated to
 * Spring Data JDBC.</p>
 */
@Component
public class LegacyDataSourceBridge {

    private final DataSource dataSource;

    public LegacyDataSourceBridge(DataSource dataSource) {
        this.dataSource = dataSource;
        DBUtil.useDataSource(dataSource);
    }

    @PreDestroy
    void detach() {
        DBUtil.clearDataSource(dataSource);
    }
}
