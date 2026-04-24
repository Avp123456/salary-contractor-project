package com.project.login.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInit implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking database schema...");
        try {
            // First add columns as nullable
            jdbcTemplate.execute("ALTER TABLE uploaded_files ADD COLUMN IF NOT EXISTS header_count INTEGER");
            jdbcTemplate.execute("ALTER TABLE uploaded_files ADD COLUMN IF NOT EXISTS trailer_count INTEGER");
            jdbcTemplate.execute("ALTER TABLE uploaded_files ADD COLUMN IF NOT EXISTS file_content BYTEA");
            jdbcTemplate.execute("ALTER TABLE uploaded_files ADD COLUMN IF NOT EXISTS contractor_id BIGINT");
            jdbcTemplate.execute("ALTER TABLE uploaded_file_columns ADD COLUMN IF NOT EXISTS actual_col_name VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE uploaded_file_columns ADD COLUMN IF NOT EXISTS parse BOOLEAN");
            jdbcTemplate.execute("ALTER TABLE uploaded_file_columns ADD COLUMN IF NOT EXISTS contractor_id BIGINT");
            jdbcTemplate.execute("ALTER TABLE file_data ADD COLUMN IF NOT EXISTS contractor_id BIGINT");
            jdbcTemplate.execute("ALTER TABLE file_data ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'Pending'");
            jdbcTemplate.execute("ALTER TABLE file_data ADD COLUMN IF NOT EXISTS structure_viewed BOOLEAN DEFAULT FALSE");
            jdbcTemplate.execute("ALTER TABLE file_data ADD COLUMN IF NOT EXISTS payslip_generated BOOLEAN DEFAULT FALSE");

            // TEMPORARY RESET SCRIPT: Reset everything to test workflow
            jdbcTemplate.execute("UPDATE file_data SET status = 'Pending', payslip_generated = FALSE, structure_viewed = FALSE");

            // Then fill existing nulls with defaults
            jdbcTemplate.execute("UPDATE uploaded_files SET header_count = 0 WHERE header_count IS NULL");
            jdbcTemplate.execute("UPDATE uploaded_files SET trailer_count = 0 WHERE trailer_count IS NULL");
            jdbcTemplate.execute("UPDATE uploaded_file_columns SET parse = TRUE WHERE parse IS NULL");

            System.out.println("Database schema updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating database schema: " + e.getMessage());
        }
    }
}
