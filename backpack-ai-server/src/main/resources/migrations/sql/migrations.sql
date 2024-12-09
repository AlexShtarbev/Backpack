--liquibase formatted sql
--changeset alex.shtarbev:1
create schema if not exists backpack;

--changeset alex.shtarbev:2
create table if not exists backpack.content (
    id uuid default gen_random_uuid() primary key,
    content text not null,
    summary text,
    context text
);

--changeset alex.shtarbev:3
create table if not exists backpack.embedding (
    content_id uuid not null primary key,
    embedding vector(1536)
);

alter table backpack.embedding add foreign key (content_id) REFERENCES backpack.content(id)
on delete cascade on update cascade ;