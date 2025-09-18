-- Drop conflicts table if it exists with wrong foreign key constraints
DROP TABLE IF EXISTS conflicts CASCADE;

-- Create conflicts table with correct foreign key references to 'events' table
CREATE TABLE IF NOT EXISTS conflicts (
    id BIGSERIAL PRIMARY KEY,
    conflict_type VARCHAR(255) NOT NULL,
    event1_id BIGINT NOT NULL,
    event2_id BIGINT,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT fk_conflict_event1 FOREIGN KEY (event1_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_conflict_event2 FOREIGN KEY (event2_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_conflicts_event1_id ON conflicts(event1_id);
CREATE INDEX IF NOT EXISTS idx_conflicts_event2_id ON conflicts(event2_id);
CREATE INDEX IF NOT EXISTS idx_conflicts_type ON conflicts(conflict_type);
