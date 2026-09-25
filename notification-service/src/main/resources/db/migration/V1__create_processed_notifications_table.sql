CREATE TABLE processed_notifications (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_booking_notification UNIQUE (booking_id, notification_type)
);
