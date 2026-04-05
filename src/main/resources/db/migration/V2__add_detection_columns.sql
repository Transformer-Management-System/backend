-- ============================================================
-- V2: Add anomaly-detection columns
-- Run against transformer_db BEFORE starting the application.
-- ============================================================

-- 1. inspections table — detection-level metadata
ALTER TABLE inspections
    ADD COLUMN IF NOT EXISTS image_level_label   VARCHAR(50),
    ADD COLUMN IF NOT EXISTS anomaly_count       INTEGER,
    ADD COLUMN IF NOT EXISTS detection_request_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS detection_metrics   JSONB;

-- 2. anomalies table — per-blob detection fields
ALTER TABLE anomalies
    ADD COLUMN IF NOT EXISTS severity       VARCHAR(50),
    ADD COLUMN IF NOT EXISTS severity_score NUMERIC,
    ADD COLUMN IF NOT EXISTS classification VARCHAR(50),
    ADD COLUMN IF NOT EXISTS area           INTEGER,
    ADD COLUMN IF NOT EXISTS centroid       JSONB,
    ADD COLUMN IF NOT EXISTS mean_delta_e   NUMERIC,
    ADD COLUMN IF NOT EXISTS peak_delta_e   NUMERIC,
    ADD COLUMN IF NOT EXISTS mean_hsv       JSONB,
    ADD COLUMN IF NOT EXISTS elongation     NUMERIC;
