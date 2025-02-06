--liquibase formatted sql
--changeset alex.shtarbev:1
create schema if not exists backpack;

--changeset alex.shtarbev:2
create table if not exists backpack.transcription (
  url text not null primary key,
  text text not null
);

--changeset alex.shtarbev:3
create table if not exists backpack.transcription_paragraph (
    id uuid default gen_random_uuid() primary key,
    url text not null,
    text text not null,
    segments jsonb DEFAULT '{}'::jsonb not null
);

ALTER TABLE backpack.transcription_paragraph
ADD FOREIGN KEY (url) REFERENCES backpack.transcription(url)
ON DELETE CASCADE ON UPDATE CASCADE;

--changeset alex.shtarbev:4
create table if not exists backpack.transcription_paragraph_embedding (
    id uuid not null primary key,
    embeddings vector(1536)
);

ALTER TABLE backpack.transcription_paragraph_embedding
ADD FOREIGN KEY (id) REFERENCES backpack.transcription_paragraph (id)
ON DELETE CASCADE ON UPDATE CASCADE;