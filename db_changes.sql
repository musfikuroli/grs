
// 2022-10-30
create table grs_dashboard_total_resolved(id integer auto_increment primary key , office_id integer, office_name varchar(500), total_count integer, resolved_count integer,expired_count integer, rate float, created_at timestamp default current_timestamp);

create table grs_current_year_statistics(id integer, total_complaint integer, total_forwarded integer,total_resolved integer, created_at timestamp default current_timestamp);


create table grs_statistics
(
    id                                 integer auto_increment primary key,
    office_id                          integer,
    year                               integer,
    month                              integer,
    total_submitted_grievance          integer,
    current_month_acceptance           integer,
    ascertain_of_last_month            integer,
    running_grievances                 integer,
    forwarded_grievances               integer,
    time_expired_grievances            integer,
    resolved_grievances                integer,
    resolve_rate                       float,
    rate_of_appealed_grievance         float,
    total_rating                       integer,
    average_rating                     float,
    appeal_total                       integer,
    appeal_current_month_acceptance    integer,
    appeal_ascertain                   integer,
    appeal_running                     integer,
    appeal_time_expired                integer,
    appeal_resolved                    integer,
    appeal_resolve_rate                float,
    sub_offices_total_grievance        integer,
    sub_offices_time_expired_grievance integer,
    sub_offices_resolved_grievance     integer,
    sub_offices_total_appeal           integer,
    sub_offices_time_expired_appeal    integer,
    sub_offices_resolved_appeal        integer,
    sub_offices_grievance_resolve_rate float,
    sub_offices_appeal_resolve_rate    float,
    created_at                         timestamp default current_timestamp
);