create table admissions_office.spring_session (
  primary_id char(36) not null,
  session_id char(36) not null,
  creation_time bigint(20) not null,
  last_access_time bigint(20) not null,
  max_inactive_interval int(11) not null,
  expiry_time bigint(20) not null,
  principal_name varchar(100) default null,
  primary key (primary_id)
) engine = INNODB,
character set utf8,
collate utf8_general_ci;

create table admissions_office.spring_session_attributes (
  session_primary_id char(36) not null,
  attribute_name varchar(200) not null,
  attribute_bytes longblob not null,
  primary key (session_primary_id, attribute_name)
) engine = INNODB,
character set utf8,
collate utf8_general_ci;

alter table admissions_office.spring_session
add unique index spring_session_ix1 (session_id);

alter table admissions_office.spring_session
add index spring_session_ix2 (expiry_time);

alter table admissions_office.spring_session
add index spring_session_ix3 (principal_name);

alter table admissions_office.spring_session_attributes
add constraint spring_session_attributes_fk foreign key (session_primary_id)
references admissions_office.spring_session (primary_id) on delete cascade;