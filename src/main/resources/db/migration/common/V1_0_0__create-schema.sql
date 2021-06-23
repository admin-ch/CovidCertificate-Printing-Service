create table certificate_pdf_data
(
    id  uuid  not null
        constraint pdf_certificate_data_pkey
            primary key,
    pdf bytea not null
);

create table certificate_print_queue_item
(
    id                      uuid        not null
        constraint certificate_print_queue_item_pkey
            primary key,
    uvci                    varchar(39) not null unique,
    status                  varchar(39) not null,
    address_line1           varchar(39),
    address_line2           varchar(39),
    address_line3           varchar(39),
    zip_code                integer,
    city                    varchar(39),
    language                varchar(39),
    certificate_pdf_data_id uuid        not null REFERENCES certificate_pdf_data (id),
    created_at              timestamp   not null default now(),
    modified_at             timestamp   not null default now()
);

CREATE INDEX certificate_print_queue_item_status_idx ON certificate_print_queue_item (status);