ALTER TABLE if exists location_history
DROP
constraint if exists LOCATIONHISTORY_FK0;
ALTER TABLE if exists drones
DROP
constraint if exists LOCATIONHISTORY_FK0;

ALTER TABLE if exists location_history
DROP
constraint if exists BOOKINGS_FK0;
ALTER TABLE if exists drones
DROP
constraint if exists BOOKINGS_FK0;
ALTER TABLE if exists rides
DROP
constraint if exists FK_RIDES;


drop table if exists location_history;
drop table if exists drones;
drop table if exists bookings;
drop table if exists rides;

create table drones
(
    id      bigint auto_increment,
    name    varchar(50) not null,
    on_ride boolean     not null default false,
    primary key (id)
);

create table location_history
(
    id         bigint auto_increment,
    drone_id   int         not null,
    coordinate varchar(50) not null,
    datetime   datetime    not null,
    primary key (id)
);

create table bookings
(
    id              bigint auto_increment,
    mars_id         int         not null,
    datetime        datetime    not null,
    pickup_location varchar(50) not null,
    destination     varchar(50) not null,
    primary key (id)
);

create table rides
(
    id              bigint auto_increment,
    mars_id         int            not null,
    drone_id        int            not null,
    pickup_location varchar(50)    not null,
    destination     varchar(50)    not null,
    fare            numeric(20, 2) not null default 0.00,
    is_completed    boolean        not null default false,
    completion_date datetime       not null default current_timestamp(0)

);

ALTER TABLE location_history
    ADD CONSTRAINT fk_location_history FOREIGN KEY (drone_id) REFERENCES drones (id);
ALTER TABLE rides
    ADD CONSTRAINT fk_rides FOREIGN KEY (drone_id) REFERENCES drones (id);