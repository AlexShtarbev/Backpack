--liquibase formatted sql
--changeset alex.shtarbev:1
create schema if not exists backpack;

--changeset alex.shtarbev:2
create table if not exists backpack.content (
    id uuid default gen_random_uuid() primary key,
    content text not null,
    summary text,
    context text,
    embedding vector(1536)
);