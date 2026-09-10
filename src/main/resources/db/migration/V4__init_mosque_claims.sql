-- ==============================================================================
-- Flyway Migration: V4 - Mosque Admin Claim Requests & Verification Schema
-- ==============================================================================

CREATE TABLE IF NOT EXISTS mosque_claim_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL REFERENCES mosques(id) ON DELETE CASCADE,
    claimant_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    official_email VARCHAR(255),
    position_in_mosque VARCHAR(100) NOT NULL, -- e.g. 'Imam', 'Committee President', 'Trustee', 'Secretary'
    proof_document_url VARCHAR(1000),         -- Utility bill, registration cert, or official letter
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
    reviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    review_comments TEXT,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_mosque_claims_status ON mosque_claim_requests(status);
CREATE INDEX IF NOT EXISTS idx_mosque_claims_mosque_id ON mosque_claim_requests(mosque_id);
CREATE INDEX IF NOT EXISTS idx_mosque_claims_claimant_id ON mosque_claim_requests(claimant_id);
