
scp  admin@115.127.88.53:/home/admin/t/ROOT.war .
/home/grs/tomcat/bin/shutdown.sh
rm -rf /home/grs/tomcat/webapps/ROOT
mv  /home/grs/tomcat/ROOT.war /home/grs/tomcat/webapps/
/home/grs/tomcat/bin/startup.sh



cp /usr/local/grs_software/apache-tomcat-8.5.85/webapps/ROOT.war /usr/local/grs_software/apache-tomcat-8.5.85/webapps/ROOT.war.bk
/usr/local/grs_software/apache-tomcat-8.5.85/bin/shutdown.sh
rm -rf /usr/local/grs_software/apache-tomcat-8.5.85/webapps/ROOT
mv  /usr/local/grs_software/apache-tomcat-8.5.85/ROOT.war /usr/local/grs_software/apache-tomcat-8.5.85/webapps/
/usr/local/grs_software/apache-tomcat-8.5.85/bin/startup.sh


cp /usr/local/grs_software/apache-tomcat-8.5.85/webapps/ROOT.war /usr/local/grs_software/apache-tomcat-8.5.85/webapps/ROOT`date +"%Y%m%d_%H%M"`.war.bk
`date +"%Y%m%d_%H%M"`

