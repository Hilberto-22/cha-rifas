create table raffles (
    id uuid primary key,
    title varchar(120) not null,
    draw_at timestamptz not null,
    number_price numeric(10, 2) not null check (number_price > 0),
    first_prize numeric(10, 2) not null,
    second_prize numeric(10, 2) not null,
    pix_key varchar(180),
    active boolean not null default true,
    created_at timestamptz not null default now()
);

create table reservations (
    id uuid primary key,
    raffle_id uuid not null references raffles(id),
    participant_name varchar(80) not null,
    phone varchar(20) not null,
    payment_method varchar(20) not null check (payment_method in ('PIX', 'DIAPER', 'CARD')),
    status varchar(20) not null check (status in ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    expires_at timestamptz not null,
    created_at timestamptz not null default now()
);

create table raffle_numbers (
    id uuid primary key,
    raffle_id uuid not null references raffles(id) on delete cascade,
    number integer not null check (number between 1 and 150),
    status varchar(20) not null check (status in ('AVAILABLE', 'RESERVED', 'CONFIRMED')),
    reservation_id uuid references reservations(id),
    constraint uk_raffle_number unique (raffle_id, number)
);

create index idx_raffle_numbers_raffle on raffle_numbers (raffle_id, number);
create index idx_reservations_expiration on reservations (status, expires_at);
