package migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class FlywayMigrationTest {

    @Test
    void existingSchemaCanBeBaselinedAtVersionOne() throws Exception {
        String url = "jdbc:h2:mem:flywayBaseline;DB_CLOSE_DELAY=-1";
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE \"flyway_schema_history\"");
        }

        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .baselineDescription("Existing schema before Flyway")
                .load();
        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "SELECT \"type\", \"version\", \"success\" "
                                + "FROM \"flyway_schema_history\" WHERE \"version\" = '1'")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getString("type")).isEqualTo("BASELINE");
            assertThat(result.getString("version")).isEqualTo("1");
            assertThat(result.getBoolean("success")).isTrue();
            assertThat(result.next()).isFalse();
        }
        assertThat(flyway.info().pending()).isEmpty();
    }
}
