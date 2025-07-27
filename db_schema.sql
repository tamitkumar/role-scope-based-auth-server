create table auth.registered_services
(
    id           bigint auto_increment
        primary key,
    service_name varchar(255)         not null,
    constraint service_name
        unique (service_name)
);

create table auth.roles
(
    id   bigint auto_increment
        primary key,
    name varchar(255) not null,
    constraint name
        unique (name)
);

create table auth.scope_requests
(
    id         bigint auto_increment
        primary key,
    scope      varchar(255)         null,
    approved   tinyint(1) default 0 null,
    service_id bigint               null,
    constraint fk_scope_requests_service
        foreign key (service_id) references auth.registered_services (id)
            on delete cascade
);

create table auth.user_info
(
    id       int auto_increment
        primary key,
    name     varchar(255) not null,
    email    varchar(255) null,
    password varchar(255) not null,
    constraint email
        unique (email),
    constraint name
        unique (name)
);

create table auth.user_roles
(
    user_id int    not null,
    role_id bigint not null,
    primary key (user_id, role_id),
    constraint user_roles_ibfk_1
        foreign key (user_id) references auth.user_info (id),
    constraint user_roles_ibfk_2
        foreign key (role_id) references auth.roles (id)
);

create index role_id
    on auth.user_roles (role_id);

CREATE TABLE auth.refresh_token (
  token_id VARCHAR(255) NOT NULL PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  expiry DATETIME NOT NULL
);