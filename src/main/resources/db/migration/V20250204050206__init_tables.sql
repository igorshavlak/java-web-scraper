create table image_entity
(
    id                     serial primary key,
    original_url           text unique not null,
    path                   text        not null,
    original_size          bigint      not null,
    size_after_compression bigint      not null
);
create index idx_images_path ON image_entity (path);

create table scraper_sessions
(
    session_id  varchar(255) primary key,
    start_url   varchar(2048) not null,
    domain      varchar(255)  not null,
    is_canceled BOOLEAN   DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
create index idx_scraper_sessions_domain ON scraper_sessions (domain);

create table visited_urls
(
    id         serial primary key,
    session_id varchar(255)  not null,
    url        varchar(2048) not null,
    depth      int           not null,
    visited_at timestamp default current_timestamp,
    constraint fk_session foreign key (session_id) references scraper_sessions (session_id) on delete cascade
);
create index idx_visited_urls_session on visited_urls (session_id);
create unique index idx_visited_urls_unique on visited_urls (session_id, url);
