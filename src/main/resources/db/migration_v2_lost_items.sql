-- ============================================================================
-- Migration Script: v2.0 - Add Lost Item Reports & Claim Association
-- Database: campusfind
-- Does NOT delete existing found_items or claims data.
-- ============================================================================

USE campusfind;

-- ----------------------------------------------------------------------------
-- 1. Create table: lost_items
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lost_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    location_lost VARCHAR(100) NOT NULL,
    date_lost DATE NOT NULL,
    date_reported TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    student_contact VARCHAR(100) NOT NULL,
    identifying_details VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    
    -- Status validation check constraint (MySQL 8+)
    CONSTRAINT chk_lost_status CHECK (status IN ('OPEN', 'MATCHED', 'RESOLVED')),
    
    -- Indexes for search and matching
    INDEX idx_lost_items_status (status),
    INDEX idx_lost_items_category (category),
    INDEX idx_lost_items_location (location_lost),
    INDEX idx_lost_items_date (date_lost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. Add optional lost_report_id foreign key to claims table if not exists
-- ----------------------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'campusfind' 
      AND TABLE_NAME = 'claims' 
      AND COLUMN_NAME = 'lost_report_id'
);

SET @alter_sql = IF(
    @col_exists = 0,
    'ALTER TABLE claims 
        ADD COLUMN lost_report_id INT NULL AFTER item_id,
        ADD CONSTRAINT fk_claims_lost_items 
            FOREIGN KEY (lost_report_id) 
            REFERENCES lost_items(id) 
            ON DELETE SET NULL 
            ON UPDATE CASCADE,
        ADD INDEX idx_claims_lost_report_id (lost_report_id)',
    'SELECT "Column lost_report_id already present in claims table" AS status'
);

PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
