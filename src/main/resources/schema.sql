CREATE TABLE IF NOT EXISTS
    ms_user (
        user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        user_name TEXT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE TABLE IF NOT EXISTS
    ms_event (
        event_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        event_name TEXT NOT NULL,
        event_venue TEXT,
        event_time TIMESTAMPTZ,
        sale_start TIMESTAMPTZ NOT NULL,
        sale_end TIMESTAMPTZ NOT NULL,
        ticket_total INT NOT NULL,
        ticket_available INT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        event_rate_persecond INT NOT NULL DEFAULT 100,
        CONSTRAINT chk_available_non_negative CHECK (ticket_available >= 0),
        CONSTRAINT chk_available_within_total CHECK (ticket_available <= ticket_total)
    );

CREATE TABLE IF NOT EXISTS
    tr_booking_token (
        token_id UUID PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES ms_user (user_id),
        event_id BIGINT NOT NULL REFERENCES ms_event (event_id),
        token_status TEXT NOT NULL DEFAULT 'ISSUED',
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT chk_token_status CHECK (token_status IN ('ISSUED', 'USED'))
    );

CREATE TABLE IF NOT EXISTS
    tr_booking (
        booking_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        token_id UUID NOT NULL UNIQUE REFERENCES tr_booking_token (token_id),
        user_id BIGINT NOT NULL REFERENCES ms_user (user_id),
        event_id BIGINT NOT NULL REFERENCES ms_event (event_id),
        quantity INT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT chk_quantity_positive CHECK (quantity >= 1)
    );

CREATE INDEX IF NOT EXISTS idx_booking_user ON tr_booking (user_id);