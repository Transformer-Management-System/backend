-- ============================================================
-- V3: Add annotated image key column to inspections
-- Stores the S3 object key of the AI-generated annotated image
-- produced by the anomaly detection service.
-- The column is nullable — it remains NULL if the detection
-- service did not produce / successfully upload an annotated image.
-- ============================================================

ALTER TABLE inspections
    ADD COLUMN IF NOT EXISTS annotated_image_key VARCHAR(500);
