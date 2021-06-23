-- Change direction of dependency. Move dependency to the dependent instead of the parent table.
-- Certificate_pdf_data should reference certificate_print_queue_item and not the other way around.
-- This allows for delete cascade.
ALTER TABLE certificate_pdf_data
    ADD COLUMN certificate_pdf_print_queue_item_id uuid UNIQUE
        REFERENCES certificate_print_queue_item (id)
            ON DELETE CASCADE;

DELETE FROM certificate_print_queue_item
WHERE status = 'PROCCESSED';

DELETE FROM certificate_pdf_data pdf_data
WHERE pdf_data.id in (
    SELECT pdf_data.id
    FROM certificate_pdf_data pdf_data
             LEFT OUTER JOIN certificate_print_queue_item item
                             ON pdf_data.id = item.certificate_pdf_data_id
    WHERE item.id is null
);

UPDATE certificate_pdf_data pdf_data
SET certificate_pdf_print_queue_item_id = (
    SELECT item.id
    FROM (
        SELECT *
        FROM certificate_print_queue_item
    ) item
    WHERE item.certificate_pdf_data_id = pdf_data.id
);

ALTER TABLE certificate_pdf_data
    ALTER COLUMN certificate_pdf_print_queue_item_id SET NOT NULL;

ALTER TABLE certificate_print_queue_item
    DROP COLUMN certificate_pdf_data_id;

