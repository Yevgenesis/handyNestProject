--liquibase formated sql

create table databasechangelog
(
    id            varchar(255) not null,
    author        varchar(255) not null,
    filename      varchar(255) not null,
    dateexecuted  timestamp    not null,
    orderexecuted integer      not null,
    exectype      varchar(10)  not null,
    md5sum        varchar(35),
    description   varchar(255),
    comments      varchar(255),
    tag           varchar(255),
    liquibase     varchar(20),
    contexts      varchar(255),
    labels        varchar(255),
    deployment_id varchar(10)
);

alter table databasechangelog
    owner to postgres;

create table databasechangeloglock
(
    id          integer not null
        primary key,
    locked      boolean not null,
    lockgranted timestamp,
    lockedby    varchar(255)
);

alter table databasechangeloglock
    owner to postgres;

create table address
(
    id      bigserial
        primary key,
    city    varchar(255),
    country varchar(255),
    street  varchar(255),
    zip     varchar(255)
);

alter table address
    owner to postgres;

create table category
(
    weight    integer not null,
    id        bigserial
        primary key,
    parent_id bigint,
    title     varchar(255)
);

alter table category
    owner to postgres;

create table handy_user
(
    is_deleted        boolean      not null,
    is_email_verified boolean      not null,
    user_rating       double precision,
    created_on        timestamp(6) not null,
    id                bigserial
        primary key,
    task_count        bigint       not null,
    updated_on        timestamp(6) not null,
    email             varchar(50)  not null
        unique,
    first_name        varchar(50)  not null,
    last_name         varchar(50)  not null,
    password          varchar(50)  not null,
    logo              varchar(255)
);

alter table handy_user
    owner to postgres;

create table performer
(
    is_available         boolean,
    is_passport_verified boolean,
    is_phone_verified    boolean,
    performer_rating     double precision,
    address_id           bigint
        unique
        constraint fk2t2b3mt3b23s1llv1pnwri6so
            references address,
    created_on           timestamp(6) not null,
    id                   bigint       not null
        primary key
        constraint fkth5vq2g0odgcd2nh2sjwlpt75
            references handy_user,
    task_count           bigint       not null,
    updated_on           timestamp(6) not null,
    phone_number         varchar(20),
    description          varchar(255)
);

alter table performer
    owner to postgres;

create table attachment
(
    id           bigserial
        primary key,
    performer_id bigint
        constraint fkdk3dom6rpgkitsyv6dpc57o90
            references performer,
    file_name    varchar(255) not null,
    type         varchar(255) not null,
    url          varchar(255) not null
);

alter table attachment
    owner to postgres;

create table performer_categories
(
    category_id  bigint not null
        constraint fk6cs63tb7k27n8raa2qe8au9ci
            references category,
    performer_id bigint not null
        constraint fkko8sula0p143lmrs0pogsw0kq
            references performer,
    primary key (category_id, performer_id)
);

alter table performer_categories
    owner to postgres;

create table user_roles
(
    user_id   bigint not null
        constraint fkfdk0r67iw3f4qyai4j3nlyv65
            references handy_user,
    role_name varchar(255)
        constraint user_roles_role_name_check
            check ((role_name)::text = ANY
                   ((ARRAY ['USER'::character varying, 'PERFORMER'::character varying, 'ADMIN'::character varying])::text[])),
    unique (user_id, role_name)
);

alter table user_roles
    owner to postgres;

create table working_time
(
    id    bigserial
        primary key,
    title varchar(255) not null
);

alter table working_time
    owner to postgres;

create table task
(
    is_publish      boolean      not null,
    price           double precision,
    address_id      bigint
        constraint fkpppqv1uxfwbht8u9nhia2s4hj
            references address,
    category_id     bigint
        constraint fkkjb4pwpo8oqc8fvkgbmiitsu9
            references category,
    created_on      timestamp(6) not null,
    id              bigserial
        primary key,
    performer_id    bigint
        constraint fk7jmo8nkpmy18rbbi2kmrlkcub
            references performer,
    updated_on      timestamp(6) not null,
    user_id         bigint
        constraint fkh7dss3iwn48xe2ddtfimxe3xa
            references handy_user,
    working_time_id bigint
        constraint fk7gxjs1ied1bicfq661s9kjqgl
            references working_time,
    description     varchar(255),
    task_status     varchar(255)
        constraint task_task_status_check
            check ((task_status)::text = ANY
                   ((ARRAY ['OPEN'::character varying, 'CANCELED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying])::text[])),
    title           varchar(255)
);

alter table task
    owner to postgres;

create table feedback
(
    created_on timestamp(6) not null,
    grade      bigint,
    id         bigserial
        primary key,
    sender_id  bigint
        constraint fk4kgmacghfrmrbfu9w6lvb559m
            references handy_user,
    task_id    bigint
        constraint fk1ltjbw3uchscc3jbw5cscwux8
            references task,
    text       varchar(255)
);

alter table feedback
    owner to postgres;

