--liquibase formatted sql
--changeset alex.shtarbev:1
create schema if not exists backpack;

create table if not exists backpack.test (
    ID UUID
)