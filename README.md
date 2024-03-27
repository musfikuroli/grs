# **GRS Project**


 
## **Prerequisites**

 Java 8, gradle-3.4.1, MySql , IntelliJ IDEA

---
##STEPS (RUN on intelij)

1. Set Gradle version  3.4.1 on intelIj
2. Set JDK version 8
3. Change Active Spring Profile
4. run from gradle task (bootRun)

---


 ## **RUN on Console**

```sh
$ gradle  build
$ gradle bootRun

```
---
  ### **PRODUCTION DEPLOY**
  1. Bypass DOPTOR  USER LOGin CODE COMMENT ( )
  2. ACTIVE PROFILE CHANGE
  3. LOGOUT USER CHANGE FROM HTML  ( admin_footer_includes.html ) 
  4. 



vi /etc/systemd/system/grs.service
```
[Unit]
Description=GRS Service

[Service]
User=nobody
# The configuration file application.properties should be here:
WorkingDirectory=/root/grs/warRun/
ExecStart=/usr/bin/java -Xmx1000m -jar -Dspring.profiles.active=dev grs.war
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target

```

systemctl daemon-reload

systemctl start grs
systemctl stop grs
systemctl restart grs
systemctl status grs


journalctl -u grs.service


=======================================================
create table grs_dashboard_total_resolved(id integer auto_increment primary key , office_id integer, office_name varchar(500), total_count integer, resolved_count integer,expired_count integer, rate float, created_at timestamp default current_timestamp);

create table grs_current_year_statistics(id integer, total_complaint integer, total_forwarded integer,total_resolved integer, created_at timestamp default current_timestamp);



# grs_live
