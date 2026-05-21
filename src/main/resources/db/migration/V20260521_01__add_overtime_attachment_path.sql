-- Migration: Add attachment_path to overtime_request
ALTER TABLE overtime_request ADD COLUMN attachment_path VARCHAR(255);
