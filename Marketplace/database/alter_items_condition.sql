USE usc_marketplace;

ALTER TABLE Items ADD COLUMN item_condition VARCHAR(32) NULL AFTER description;
