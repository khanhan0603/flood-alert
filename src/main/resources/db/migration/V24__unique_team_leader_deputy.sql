ALTER TABLE rescue_teams
ADD CONSTRAINT uk_rescue_teams_leader
UNIQUE (leader_id);

ALTER TABLE rescue_teams
ADD CONSTRAINT uk_rescue_teams_deputy
UNIQUE (deputy_leader_id);