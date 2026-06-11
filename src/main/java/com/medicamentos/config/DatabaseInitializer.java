package com.medicamentos.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
public class DatabaseInitializer {

    @Bean
    ApplicationRunner initializeDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                log.info("Verificando integridad de la base de datos...");
                fixAuditLogsUserEmailColumn(jdbcTemplate);
            } catch (Exception e) {
                log.error("Error durante inicialización de BD: {}", e.getMessage(), e);
            }
        };
    }

    private void fixAuditLogsUserEmailColumn(JdbcTemplate jdbcTemplate) {
        try {
            // Check if user_email column exists and its type
            String query = """
                    SELECT data_type
                    FROM information_schema.columns
                    WHERE table_name = 'audit_logs' AND column_name = 'user_email'
                    """;
            var result = jdbcTemplate.query(query, rs -> {
                if (rs.next()) {
                    return rs.getString("data_type");
                }
                return null;
            });

            if (result != null && result.equalsIgnoreCase("bytea")) {
                log.warn("Detectada columna user_email como bytea. Convirtiendo a VARCHAR...");
                String fixSql = """
                        ALTER TABLE audit_logs
                        ALTER COLUMN user_email TYPE VARCHAR(120)
                        USING CASE
                          WHEN user_email IS NULL THEN NULL
                          ELSE convert_from(user_email, 'UTF8')
                        END
                        """;
                jdbcTemplate.execute(fixSql);
                log.info("✓ Columna user_email corregida a VARCHAR(120)");
            } else if (result != null) {
                log.debug("Columna user_email OK: {}", result);
            }
        } catch (Exception e) {
            log.warn("No se pudo verificar/corregir audit_logs.user_email: {}", e.getMessage());
        }
    }
}
