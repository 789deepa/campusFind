-- ============================================================================
-- Database Schema for CampusFind: Privacy-First Campus Lost & Found System
-- Compatible with MySQL 8.0+
-- ============================================================================

CREATE DATABASE IF NOT EXISTS campusfind;
USE campusfind;

-- Drop dependent tables first to maintain foreign key integrity
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS lost_items;
DROP TABLE IF EXISTS found_items;

-- ----------------------------------------------------------------------------
-- Table: found_items
-- Description: Stores items found across the campus.
-- Privacy Rule: The 'private_verification_clue' is confidential and must
--               never be displayed in public item listings or ordinary logs.
-- ----------------------------------------------------------------------------
CREATE TABLE found_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    location_found VARCHAR(100) NOT NULL,
    date_found DATE NOT NULL,
    date_reported TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    reported_by VARCHAR(100) NOT NULL,
    private_verification_clue VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    
    -- Status validation check constraint (MySQL 8+)
    CONSTRAINT chk_item_status CHECK (status IN ('AVAILABLE', 'CLAIM_PENDING', 'RETURNED')),
    
    -- Indexes for high-frequency search and filter queries
    INDEX idx_found_items_status (status),
    INDEX idx_found_items_category (category),
    INDEX idx_found_items_location (location_found),
    INDEX idx_found_items_date (date_found)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: lost_items
-- Description: Stores reports filed by students who lost personal belongings.
-- Privacy Rule: 'identifying_details' (secret marks/features) is confidential
--               and used strictly to verify matches against found items.
-- ----------------------------------------------------------------------------
CREATE TABLE lost_items (
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
    
    -- Status validation check constraint
    CONSTRAINT chk_lost_status CHECK (status IN ('OPEN', 'MATCHED', 'RESOLVED')),
    
    -- Indexes for search and matching queries
    INDEX idx_lost_items_status (status),
    INDEX idx_lost_items_category (category),
    INDEX idx_lost_items_location (location_lost),
    INDEX idx_lost_items_date (date_lost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: claims
-- Description: Stores claims filed by students attempting to reclaim items.
-- Privacy Rule: 'verification_response' holds the claimant's answer to the
--               private clue and is only accessible by staff during review.
-- ----------------------------------------------------------------------------
CREATE TABLE claims (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    lost_report_id INT NULL,
    claimant_name VARCHAR(100) NOT NULL,
    claimant_contact VARCHAR(100) NOT NULL,
    claim_description TEXT NOT NULL,
    verification_response VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP NULL,
    
    -- Foreign keys to ensure referential integrity
    CONSTRAINT fk_claims_found_items 
        FOREIGN KEY (item_id) 
        REFERENCES found_items(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,

    CONSTRAINT fk_claims_lost_items 
        FOREIGN KEY (lost_report_id) 
        REFERENCES lost_items(id) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
        
    -- Status validation check constraint
    CONSTRAINT chk_claim_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    
    -- Indexes for joining and claim review filtering
    INDEX idx_claims_item_id (item_id),
    INDEX idx_claims_lost_report_id (lost_report_id),
    INDEX idx_claims_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
