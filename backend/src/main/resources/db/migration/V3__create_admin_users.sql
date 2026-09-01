create table admin_users (
    id uuid primary key,
    username varchar(60) not null unique,
    password_hash varchar(100) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);
