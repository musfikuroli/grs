
update complaints set is_safety_net=true where id in (select grievance_id from safety_net_grievance);

alter table safety_net_program
    modify code varchar(50) null;



create table safety_net_sub_program(
                                       id integer AUTO_INCREMENT primary key ,
                                       name_en varchar(200),
                                       name_bn varchar(200),
                                       safety_net_program_id integer not null ,
                                       status bool default true
);


alter table safety_net_grievance add column sub_type varchar(200);
alter table complaints add column is_safety_net bool default false;

insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10005, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10005, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10005, true);

insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10006, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10006, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10006, true);


insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10007, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10007, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10007, true);

insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10008, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10008, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10008, true);


insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10009, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10009, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10009, true);


insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10010, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10010, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10010, true);


insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10011, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10011, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10011, true);


insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10012, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10012, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10012, true);


insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10013, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10013, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10013, true);


insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Exclusion Error','বর্জন ত্রুটি',10014, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Inclusion Error','অন্তর্ভুক্তি ত্রুটি',10014, true);
insert into safety_net_sub_program(name_en, name_bn, safety_net_program_id, status) values('Money not Received','টাকা প্রাপ্ত হয়নি',10014, true);