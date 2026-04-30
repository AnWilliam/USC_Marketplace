USE usc_marketplace;

ALTER TABLE Items ADD COLUMN photo_path VARCHAR(500) NULL AFTER item_condition;
