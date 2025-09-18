-- Fix foreign key constraints in conflicts table to reference 'events' table instead of 'event' table

-- Drop the conflicts table completely (this will also drop the foreign key constraints)
DROP TABLE IF EXISTS conflicts CASCADE;

-- Recreate the conflicts table with correct foreign key references
CREATE TABLE conflicts (
    id BIGSERIAL PRIMARY KEY,
    conflict_type VARCHAR(255) NOT NULL,
    event1_id BIGINT NOT NULL,
    event2_id BIGINT,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT fk_conflict_event1 FOREIGN KEY (event1_id) REFERENCES events(id),
    CONSTRAINT fk_conflict_event2 FOREIGN KEY (event2_id) REFERENCES events(id)
);

-- Add indexes for better performance
CREATE INDEX idx_conflicts_event1_id ON conflicts(event1_id);
CREATE INDEX idx_conflicts_event2_id ON conflicts(event2_id);
CREATE INDEX idx_conflicts_type ON conflicts(conflict_type);
