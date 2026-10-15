-- ============================================================================
-- CampusFind: SQL Practice & Interview Query Reference
-- Database: campusfind
-- Schema Tables: found_items, claims
-- ============================================================================

USE campusfind;

-- ----------------------------------------------------------------------------
-- Query 1: List all available items open for claiming
-- Concept: SELECT, WHERE, IN
-- Question: Which items are currently in the repository waiting for their owners?
-- ----------------------------------------------------------------------------
SELECT 
    id, 
    item_name, 
    category, 
    location_found, 
    date_found, 
    status
FROM found_items
WHERE status IN ('AVAILABLE', 'CLAIM_PENDING')
ORDER BY date_found DESC;

-- ----------------------------------------------------------------------------
-- Query 2: Search items by keyword (case-insensitive substring)
-- Concept: LIKE pattern matching with wildcards (%)
-- Question: Find all items that mention 'Casio' or 'calculator' in their name or description.
-- ----------------------------------------------------------------------------
SELECT 
    id, 
    item_name, 
    category, 
    description, 
    location_found
FROM found_items
WHERE item_name LIKE '%calculator%' 
   OR description LIKE '%calculator%'
ORDER BY id DESC;

-- ----------------------------------------------------------------------------
-- Query 3: Filter items by specific category
-- Concept: WHERE with exact equality
-- Question: Retrieve all electronic gadgets lost on campus.
-- ----------------------------------------------------------------------------
SELECT 
    id, 
    item_name, 
    location_found, 
    date_found, 
    status
FROM found_items
WHERE category = 'Electronics'
ORDER BY date_found DESC;

-- ----------------------------------------------------------------------------
-- Query 4: Filter items by location
-- Concept: WHERE LIKE for partial campus zone matching
-- Question: What items were discovered inside the Central Library?
-- ----------------------------------------------------------------------------
SELECT 
    id, 
    item_name, 
    location_found, 
    reported_by, 
    status
FROM found_items
WHERE location_found LIKE '%Library%'
ORDER BY date_found DESC;

-- ----------------------------------------------------------------------------
-- Query 5: Finding items reported within a specific date range
-- Concept: BETWEEN operator on DATE columns
-- Question: Which items were found between October 10, 2026 and October 13, 2026?
-- ----------------------------------------------------------------------------
SELECT 
    id, 
    item_name, 
    category, 
    location_found, 
    date_found
FROM found_items
WHERE date_found BETWEEN '2026-10-10' AND '2026-10-13'
ORDER BY date_found ASC;

-- ----------------------------------------------------------------------------
-- Query 6: Counting items grouped by their lifecycle status
-- Concept: COUNT(*), GROUP BY
-- Question: How many items are AVAILABLE, CLAIM_PENDING, or RETURNED?
-- ----------------------------------------------------------------------------
SELECT 
    status, 
    COUNT(*) AS item_count
FROM found_items
GROUP BY status
ORDER BY item_count DESC;

-- ----------------------------------------------------------------------------
-- Query 7: Counting items by category
-- Concept: Aggregation with COUNT and ORDER BY
-- Question: What category of items is most frequently lost and found on campus?
-- ----------------------------------------------------------------------------
SELECT 
    category, 
    COUNT(*) AS total_reported
FROM found_items
GROUP BY category
ORDER BY total_reported DESC;

-- ----------------------------------------------------------------------------
-- Query 8: Finding categories with more than 1 reported item
-- Concept: GROUP BY with HAVING filter
-- Question: Which categories have multiple reported items?
-- ----------------------------------------------------------------------------
SELECT 
    category, 
    COUNT(*) AS item_count
FROM found_items
GROUP BY category
HAVING COUNT(*) > 1
ORDER BY item_count DESC;

-- ----------------------------------------------------------------------------
-- Query 9: Listing claims with their associated item names using INNER JOIN
-- Concept: INNER JOIN on foreign key relationship
-- Question: For every claim, who is the claimant and which item did they claim?
-- ----------------------------------------------------------------------------
SELECT 
    c.id AS claim_id,
    c.claimant_name,
    c.claimant_contact,
    c.status AS claim_status,
    i.id AS item_id,
    i.item_name,
    i.location_found,
    c.submitted_at
FROM claims c
INNER JOIN found_items i ON c.item_id = i.id
ORDER BY c.submitted_at DESC;

-- ----------------------------------------------------------------------------
-- Query 10: Listing pending claims awaiting review
-- Concept: JOIN with WHERE filtering
-- Question: Which claims are waiting for staff review right now?
-- ----------------------------------------------------------------------------
SELECT 
    c.id AS claim_id,
    c.claimant_name,
    c.claim_description,
    i.item_name,
    i.location_found,
    c.submitted_at
FROM claims c
INNER JOIN found_items i ON c.item_id = i.id
WHERE c.status = 'PENDING'
ORDER BY c.submitted_at ASC;

-- ----------------------------------------------------------------------------
-- Query 11: Finding items that have never received any claims
-- Concept: LEFT JOIN with IS NULL check (Anti-Join pattern)
-- Question: Which found items currently have zero claims filed against them?
-- ----------------------------------------------------------------------------
SELECT 
    i.id AS item_id,
    i.item_name,
    i.category,
    i.location_found,
    i.date_found
FROM found_items i
LEFT JOIN claims c ON i.id = c.item_id
WHERE c.id IS NULL
ORDER BY i.date_found DESC;

-- ----------------------------------------------------------------------------
-- Query 12: Finding items that have an APPROVED claim
-- Concept: INNER JOIN with status constraint
-- Question: Which items have been successfully verified and approved for return?
-- ----------------------------------------------------------------------------
SELECT 
    i.id AS item_id,
    i.item_name,
    i.status AS item_status,
    c.id AS approved_claim_id,
    c.claimant_name,
    c.reviewed_at
FROM found_items i
INNER JOIN claims c ON i.id = c.item_id
WHERE c.status = 'APPROVED'
ORDER BY c.reviewed_at DESC;

-- ----------------------------------------------------------------------------
-- Query 13: Counting returned items
-- Concept: COUNT with WHERE clause
-- Question: How many items have successfully been returned to students?
-- ----------------------------------------------------------------------------
SELECT 
    COUNT(*) AS total_returned_items
FROM found_items
WHERE status = 'RETURNED';

-- ----------------------------------------------------------------------------
-- Query 14: Finding the most frequently reported location (Hotspot analysis)
-- Concept: GROUP BY, ORDER BY DESC, LIMIT
-- Question: What campus location has the highest concentration of lost items?
-- ----------------------------------------------------------------------------
SELECT 
    location_found, 
    COUNT(*) AS total_found
FROM found_items
GROUP BY location_found
ORDER BY total_found DESC
LIMIT 1;

-- ----------------------------------------------------------------------------
-- Query 15: Summary of claim statuses with percentages
-- Concept: Conditional aggregation (SUM CASE WHEN), COUNT, arithmetic calculation
-- Question: What is the overall breakdown of claim outcomes (Approved vs Rejected vs Pending)?
-- ----------------------------------------------------------------------------
SELECT 
    COUNT(*) AS total_claims_filed,
    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_claims,
    SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_claims,
    SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_claims,
    ROUND(SUM(CASE WHEN status = 'APPROVED' THEN 1.0 ELSE 0 END) / COUNT(*) * 100, 2) AS approval_rate_pct
FROM claims;

-- ----------------------------------------------------------------------------
-- Query 16: List active lost-item reports
-- Concept: SELECT, WHERE IN, DATE ordering
-- Question: Which student lost reports are currently active and unresolved?
-- ----------------------------------------------------------------------------
SELECT 
    id, 
    item_name, 
    category, 
    location_lost, 
    date_lost, 
    student_name, 
    status
FROM lost_items
WHERE status IN ('OPEN', 'MATCHED')
ORDER BY date_lost DESC;

-- ----------------------------------------------------------------------------
-- Query 17: Cross-Entity Matching Analysis
-- Concept: INNER JOIN on Category with chronological filter (date_found >= date_lost)
-- Question: What available found items could potentially match open lost reports?
-- ----------------------------------------------------------------------------
SELECT 
    l.id AS lost_id,
    l.item_name AS lost_item,
    l.student_name,
    l.location_lost,
    l.date_lost,
    f.id AS found_id,
    f.item_name AS found_item,
    f.location_found,
    f.date_found,
    f.status AS found_status
FROM lost_items l
INNER JOIN found_items f 
    ON l.category = f.category
   AND f.date_found >= l.date_lost
WHERE l.status = 'OPEN' 
  AND f.status IN ('AVAILABLE', 'CLAIM_PENDING')
ORDER BY l.id, f.date_found ASC;

-- ----------------------------------------------------------------------------
-- Query 18: Claims linked to lost reports with three-table JOIN
-- Concept: Three-way INNER JOIN (claims, found_items, lost_items)
-- Question: Which claims were submitted with a linked lost-item report, and what are their statuses?
-- ----------------------------------------------------------------------------
SELECT 
    c.id AS claim_id,
    c.claimant_name,
    c.status AS claim_status,
    f.id AS found_id,
    f.item_name AS found_item,
    f.status AS found_status,
    l.id AS lost_id,
    l.item_name AS lost_item,
    l.status AS lost_status
FROM claims c
INNER JOIN found_items f ON c.item_id = f.id
INNER JOIN lost_items l ON c.lost_report_id = l.id
ORDER BY c.id DESC;

-- ----------------------------------------------------------------------------
-- Query 19: Comprehensive System-Wide KPI Dashboard
-- Concept: Subqueries in SELECT for multi-table KPI aggregation
-- Question: Display a single-row executive summary of lost, found, and claim counts.
-- ----------------------------------------------------------------------------
SELECT 
    (SELECT COUNT(*) FROM found_items) AS total_found_reported,
    (SELECT COUNT(*) FROM found_items WHERE status = 'AVAILABLE') AS available_found_items,
    (SELECT COUNT(*) FROM found_items WHERE status = 'RETURNED') AS returned_items,
    (SELECT COUNT(*) FROM lost_items) AS total_lost_reported,
    (SELECT COUNT(*) FROM lost_items WHERE status = 'OPEN') AS open_lost_reports,
    (SELECT COUNT(*) FROM lost_items WHERE status = 'RESOLVED') AS resolved_lost_reports,
    (SELECT COUNT(*) FROM claims WHERE status = 'PENDING') AS pending_claims;

