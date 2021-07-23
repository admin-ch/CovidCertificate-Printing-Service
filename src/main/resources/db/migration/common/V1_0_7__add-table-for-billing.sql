create table billing_kpi
(
    id uuid not null primary key,
    canton_code_sender varchar(2),
    processed_at timestamp
);