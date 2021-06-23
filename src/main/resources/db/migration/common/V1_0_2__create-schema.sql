ALTER TABLE certificate_print_queue_item
    ALTER COLUMN status SET DATA TYPE varchar(20);

ALTER TABLE certificate_print_queue_item
    ALTER COLUMN address_line1 SET DATA TYPE varchar(200);

ALTER TABLE certificate_print_queue_item
    ALTER COLUMN address_line2 SET DATA TYPE varchar(200);

ALTER TABLE certificate_print_queue_item
    ALTER COLUMN city SET DATA TYPE varchar(200);

ALTER TABLE certificate_print_queue_item
    ALTER COLUMN language SET DATA TYPE varchar(2);