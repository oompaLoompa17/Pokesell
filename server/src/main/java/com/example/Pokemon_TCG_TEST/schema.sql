CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    spreadsheet_id VARCHAR(255),
    telegram_chat_id VARCHAR(255),  
    telegram_subscription_code VARCHAR(36)
);

CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE (user_id, card_id)
);

-- Listings table: Stores cards for sale
CREATE TABLE listings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    card_name VARCHAR(255) NOT NULL,
    card_set VARCHAR(255) NOT NULL,
    card_number VARCHAR(50) NOT NULL,
    overall_grade DOUBLE,
    starting_price DOUBLE,
    buyout_price DOUBLE,
    sold_price DOUBLE DEFAULT NULL,
    listing_type VARCHAR(50),
    auction_start DATETIME,
    auction_end DATETIME,
    sold_date DATETIME DEFAULT NULL,
    status VARCHAR(50),
    front_image MEDIUMBLOB,
    back_image MEDIUMBLOB
);

-- Bids table: Stores bids for auction listings
CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    bid_amount DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (listing_id) REFERENCES listings(id),
    FOREIGN KEY (bidder_id) REFERENCES users(id)
);

-- -- Offers table: Stores counter-offers for fixed-price listings
-- CREATE TABLE offers (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     listing_id BIGINT NOT NULL,
--     buyer_id BIGINT NOT NULL,
--     offer_price DECIMAL(10,2) NOT NULL,
--     status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (listing_id) REFERENCES listings(id),
--     FOREIGN KEY (buyer_id) REFERENCES users(id)
-- );

