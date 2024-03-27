DELIMITER //

CREATE FUNCTION getNextSequence ( mobile_no varchar(20) )
    RETURNS INT

BEGIN

    DECLARE seq INT;

    SET seq = 0;

select sequence into seq from grs_tracking_sequence where mobile_number=mobile_no;

IF seq is null or seq =0 THEN
        set seq = 1;
insert into grs_tracking_sequence(mobile_number, sequence) value (mobile_no, seq);
else
        set seq = seq+1;
update grs_tracking_sequence set sequence=seq where mobile_number=mobile_no;
end if;
RETURN seq;

END; //

DELIMITER ;


create table grs_tracking_sequence(
                                      id integer primary key auto_increment,
                                      mobile_number varchar(20) not null ,
                                      sequence integer not null
);

select * from grs_tracking_sequence;

SELECT getNextSequence ('01715102365');