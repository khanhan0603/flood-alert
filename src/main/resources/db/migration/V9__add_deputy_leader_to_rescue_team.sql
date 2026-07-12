ALTER TABLE rescue_teams
ADD COLUMN deputy_leader_id UUID;

ALTER TABLE rescue_teams
ADD CONSTRAINT fk_rescue_team_deputy_leader
FOREIGN KEY (deputy_leader_id)
REFERENCES users(id);