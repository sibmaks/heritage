alter table person
    add column if not exists biography varchar(4000);

alter table marriage
    add column if not exists relationship_type varchar(32) not null default 'MARRIAGE';
