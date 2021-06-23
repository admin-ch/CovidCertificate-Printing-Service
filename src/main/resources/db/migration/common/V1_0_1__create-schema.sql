ALTER TABLE certificate_print_queue_item
    DROP COLUMN address_line3;

ALTER TABLE certificate_print_queue_item
    ADD COLUMN canton_code_sender varchar(2);