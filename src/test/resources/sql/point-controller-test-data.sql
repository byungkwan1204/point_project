DELETE FROM points_history;
DELETE FROM points;

ALTER TABLE points ALTER COLUMN point_key RESTART WITH 1;
ALTER TABLE points_history ALTER COLUMN history_key RESTART WITH 1;

--- 포인트 적립 취소, 사용 테스트
INSERT INTO points (user_key, status, reward_type, total_amount, remain_amount, expired_at)
VALUES (1, 'ACTIVE', 'MANUAL', 1000, 1000, '2026-04-01 00:00:00');

INSERT INTO points_history (point_key, action_type, amount)
VALUES (1, 'SAVE', 1000);

INSERT INTO points (user_key, status, reward_type, total_amount, remain_amount, expired_at)
VALUES (1, 'ACTIVE', 'MANUAL', 500, 500, '2026-04-01 00:00:00');

INSERT INTO points_history (point_key, action_type, amount)
VALUES (2, 'SAVE', 500);



--- 포인트 사용 취소 테스트
INSERT INTO points (user_key, status, reward_type, total_amount, remain_amount, expired_at)
VALUES (2, 'ACTIVE', 'MANUAL', 500, 0, '2026-04-02 00:00:00');

INSERT INTO points (user_key, status, reward_type, total_amount, remain_amount, expired_at)
VALUES (2, 'EXPIRED', 'MANUAL', 300, 200, '2025-04-02 00:00:00');

INSERT INTO points_history (point_key, action_type, amount)
VALUES (3, 'SAVE', 500);

INSERT INTO points_history (point_key, action_type, amount)
VALUES (4, 'SAVE', 300);

INSERT INTO points_history (point_key, action_type, amount, order_key)
VALUES (3, 'USE', 500, 1234);

INSERT INTO points_history (point_key, action_type, amount, order_key)
VALUES (4, 'USE', 100, 1234);