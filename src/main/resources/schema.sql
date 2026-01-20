alter table if exists person drop column if exists birth_place;
alter table if exists person drop column if exists first_name;
alter table if exists person drop column if exists last_name;
alter table if exists marriage drop column if exists registration_place;
alter table if exists death drop column if exists death_place;
drop table if exists name;
drop table if exists surname;
