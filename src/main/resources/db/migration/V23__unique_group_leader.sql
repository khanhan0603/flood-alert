ALTER TABLE rescue_groups
ADD CONSTRAINT uk_rescue_groups_leader
UNIQUE (leader_id);