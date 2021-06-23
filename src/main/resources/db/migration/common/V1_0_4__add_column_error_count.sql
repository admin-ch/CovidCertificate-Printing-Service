ALTER TABLE certificate_print_queue_item
    ADD COLUMN error_count integer default 0;