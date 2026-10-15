-- ============================================================================
-- Sample Data for CampusFind System
-- Realistic campus lost-and-found items with private clues and claims
-- ============================================================================

USE campusfind;

-- Clean existing data
DELETE FROM claims;
DELETE FROM found_items;

-- Reset Auto-Increment
ALTER TABLE claims AUTO_INCREMENT = 1;
ALTER TABLE found_items AUTO_INCREMENT = 1;

-- ----------------------------------------------------------------------------
-- Inserting Found Items
-- ----------------------------------------------------------------------------
INSERT INTO found_items (item_name, category, description, location_found, date_found, reported_by, private_verification_clue, status)
VALUES
(
    'Dell Precision Laptop Charger',
    'Electronics',
    'Black 130W USB-C barrel adapter found plugged near table 4.',
    'Central Library - 2nd Floor',
    '2026-10-10',
    'Aarav Sharma (Library Attendant)',
    'Has blue electrical tape near the barrel connector and initials AS scratched on brick',
    'AVAILABLE'
),
(
    'Scientific Calculator Casio fx-991EX',
    'Electronics',
    'White and black scientific calculator left on exam hall desk.',
    'Academic Block A - Hall 101',
    '2026-10-11',
    'Prof. Ramanathan',
    'QR code sticker on battery flap is slightly peeling; student ID ending in 442 faintly written in pencil inside cover',
    'CLAIM_PENDING'
),
(
    'Blue Hydro Flask Water Bottle',
    'Others',
    'Matte blue 32oz insulated metal water bottle with several stickers.',
    'Student Cafeteria',
    '2026-10-12',
    'Pooja Nair (Student Council)',
    'Includes a specific sticker of Rick and Morty and a dent at the bottom rim',
    'AVAILABLE'
),
(
    'Brown Leather Bifold Wallet',
    'Accessories',
    'Genuine brown leather wallet containing some cash and student transit card.',
    'Sports Complex - Basketball Court',
    '2026-10-13',
    'Coach Sandeep',
    'Driver license inside belongs to state DL and contains a family photo behind the zip pocket',
    'AVAILABLE'
),
(
    'Campus Student ID Card',
    'Documents',
    'Standard university RFID smart identity card found on stairway.',
    'Hostel 4 - Main Lobby',
    '2026-10-14',
    'Security Guard Vikram',
    'Student Roll Number: 23BCSE108 (Name hidden on front due to worn lamination)',
    'RETURNED'
),
(
    'Titan Fastrack Silver Wristwatch',
    'Accessories',
    'Silver analog metallic watch with black dial.',
    'Lecture Hall Complex - Room 204',
    '2026-10-09',
    'Dr. Sunita Verma',
    'Back casing has anniversary engraving date 15-08-2022 and faint scratch at the 4 oclock marker',
    'AVAILABLE'
);

-- ----------------------------------------------------------------------------
-- Inserting Sample Claims
-- ----------------------------------------------------------------------------
-- Item #2 (Casio Calculator) has a pending claim
INSERT INTO claims (item_id, claimant_name, claimant_contact, claim_description, verification_response, status, submitted_at, reviewed_at)
VALUES
(
    2,
    'Kavya Patel',
    'kavya.patel@campus.edu / 9876543210',
    'I left my ClassWiz calculator right after the Mathematics III mid-term in hall 101.',
    'Inside the sliding hard cover, my roll number last 3 digits 442 are written in HB pencil.',
    'PENDING',
    '2026-10-12 11:30:00',
    NULL
);

-- Item #5 (Student ID) was claimed, approved, and marked returned
INSERT INTO claims (item_id, claimant_name, claimant_contact, claim_description, verification_response, status, submitted_at, reviewed_at)
VALUES
(
    5,
    'Rohan Deshmukh',
    'rohan.d@campus.edu / 9822011223',
    'Dropped my ID while walking up the stairs to room 302 in Hostel 4.',
    'Roll number 23BCSE108, branch CSE, year 2.',
    'APPROVED',
    '2026-10-14 09:15:00',
    '2026-10-14 10:00:00'
);
