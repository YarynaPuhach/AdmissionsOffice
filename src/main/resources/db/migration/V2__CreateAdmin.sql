insert into user (user_id, first_name, last_name, email, password, active)
    values (1, 'Admissions Office', 'Administrator', 'admissions.office.info@gmail.com', '$2a$08$OkqR9ogYtRouCIFmOTznwOria8hQvg5rJBhMdAl.BCxs2j5CXSQhy', true);

insert into access_level (user_id, access_levels)
    values (1, 'ADMIN');