alter table reservations drop constraint if exists reservations_status_check;
alter table reservations
    add constraint reservations_status_check
    check (status in ('PENDING', 'PAYMENT_REPORTED', 'CONFIRMED', 'CANCELLED', 'EXPIRED'));
alter table reservations add column payment_reported_at timestamptz;
