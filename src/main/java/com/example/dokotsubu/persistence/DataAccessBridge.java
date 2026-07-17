package com.example.dokotsubu.persistence;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/**
 * Spring管理外の既存Servlet/Logicから、Spring Data JDBCのBeanへ接続する移行用ブリッジ。
 */
@Component
public class DataAccessBridge {
    private static volatile SpringDataJdbcGateway current;

    private final SpringDataJdbcGateway gateway;

    public DataAccessBridge(SpringDataJdbcGateway gateway) {
        this.gateway = gateway;
        current = gateway;
    }

    public static SpringDataJdbcGateway get() {
        SpringDataJdbcGateway gateway = current;
        if (gateway == null) {
            throw new IllegalStateException("Spring Data JDBCが初期化されていません");
        }
        return gateway;
    }

    @PreDestroy
    void detach() {
        if (current == gateway) {
            current = null;
        }
    }
}
