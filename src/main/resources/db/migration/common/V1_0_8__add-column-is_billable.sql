alter table billing_kpi add column is_billable boolean;

update billing_kpi set is_billable = true where uvci is not null;
