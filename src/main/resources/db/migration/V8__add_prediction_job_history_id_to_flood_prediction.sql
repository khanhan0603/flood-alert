-- Thêm cột liên kết với lịch sử chạy AI
ALTER TABLE flood_predictions
ADD COLUMN prediction_job_history_id UUID;

-- Tạo khóa ngoại
ALTER TABLE flood_predictions
ADD CONSTRAINT fk_flood_prediction_job_history
FOREIGN KEY (prediction_job_history_id)
REFERENCES prediction_job_history(id);

-- Tạo index để tăng tốc truy vấn theo phiên chạy
CREATE INDEX idx_flood_prediction_job_history
ON flood_predictions(prediction_job_history_id);