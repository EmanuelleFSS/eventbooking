CREATE TABLE bookings (
      id BIGSERIAL PRIMARY KEY,
      event_id BIGINT NOT NULL,
      customer_email VARCHAR(255) NOT NULL,
      seats_booked INTEGER NOT NULL,
      status VARCHAR(20) NOT NULL,
      version BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);