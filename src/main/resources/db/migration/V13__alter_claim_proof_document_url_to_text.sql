-- Flyway Migration V13: Expand proof_document_url from VARCHAR(1000) to TEXT
ALTER TABLE mosque_claim_requests ALTER COLUMN proof_document_url TYPE TEXT;

