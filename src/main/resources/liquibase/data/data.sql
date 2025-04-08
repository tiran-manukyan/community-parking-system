-- Set the schema
SET
search_path TO parking_system;

-- Insert users
-- Password hash represents 'your_pass' for both users
INSERT INTO users (email, password_hash)
VALUES ('alice@example.com', '$2a$10$sjaTKGXFYDS8OoT.ZOC.MuGEHC1H3GyCMshpNQyyh7eMHjAyRZfDe'),
       ('bob@example.com', '$2a$10$sjaTKGXFYDS8OoT.ZOC.MuGEHC1H3GyCMshpNQyyh7eMHjAyRZfDe');

-- Insert communities
INSERT INTO communities (name)
VALUES ('Green Community'),
       ('Blue Community'),
       ('Red Community'),
       ('VIP clients of Wets Plaza');

-- Insert buildings
INSERT INTO buildings (name, address)
VALUES ('Central Tower', '123 Main St'),
       ('West Plaza', '456 Side Ave'),
       ('Downtown Garage', '123 Main St'),
       ('City Center Parking', '456 Elm St'),
       ('Westside Lot', '789 Oak Ave');

-- Associate users with communities (many-to-many)
-- Alice is part of Green and Blue
-- Bob is part of Blue, Red, and VIP
INSERT INTO users_communities (user_id, community_id)
VALUES (1, 1), -- Alice -> Green
       (1, 2), -- Alice -> Blue
       (2, 2), -- Bob   -> Blue
       (2, 3), -- Bob   -> Red
       (2, 4); -- Bob   -> VIP

-- Associate communities with buildings (many-to-many)
-- Shows which communities have access to which buildings
INSERT INTO communities_buildings (community_id, building_id)
VALUES (1, 1), -- Green Community -> Central Tower
       (2, 2), -- Blue Community  -> West Plaza
       (3, 3), -- Red Community   -> Downtown Garage
       (4, 2); -- VIP Clients     -> West Plaza

-- Insert parking spots
-- Each spot belongs to a building and optionally to a community
INSERT INTO parking_spot (spot_number, building_id, owner_community_id)
VALUES (101, 1, 1),    -- Spot 101 in Central Tower owned by Green
       (102, 1, 1),    -- Spot 102 in Central Tower owned by Green
       (201, 2, 2),    -- Spot 201 in West Plaza owned by Blue
       (202, 2, 2),    -- Spot 202 in West Plaza owned by Blue
       (300, 4, NULL), -- Spot 300 in City Center, public
       (303, 4, NULL), -- Spot 303 in City Center, public
       (401, 5, NULL), -- Spot 401 in Westside Lot, public
       (405, 5, NULL), -- Spot 405 in Westside Lot, public
       (406, 5, 3);    -- Spot 406 in Westside Lot owned by Red

-- Insert spot bookings
-- Represents reserved usage of a parking spot by a user
INSERT INTO spot_booking (user_id, spot_id, start_time, end_time, status, parking_cost)
VALUES (1, 1, '2025-04-09 10:00:00', '2025-04-09 12:00:00', 'BOOKED', 12.00), -- Alice
       (2, 3, '2025-04-10 08:00:00', '2025-04-10 10:00:00', 'USED', 10.00);   -- Bob

-- Insert parking sessions
-- Actual session when car is parked, linked to booking
INSERT INTO parking_session (user_id, spot_id, booking_id, start_time, end_time, plate_number, is_pre_booked)
VALUES (1, 1, 1, '2025-04-09 10:05:00', NULL, 'ABC123', TRUE), -- Alice's ongoing session
       (2, 3, 2, '2025-04-10 08:10:00', '2025-04-10 09:50:00', 'XYZ789', TRUE); -- Bob's completed session
