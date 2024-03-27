call "C:/Program Files/gradle-3.4.1/bin/gradle" clean
# call "C:/Program Files/gradle-3.4.1/bin/gradle" war
call "C:/Program Files/gradle-3.4.1/bin/gradle" build
# cd "build\libs\"
# rename "grs.war" "ROOT.war"
# cd "..\..\"
# call plink root@192.168.10.167 -pw IstL@09#1043 /root/grs/apache-tomcat-8.5.34/bin/shutdown.sh
# call pscp -P 22 -pw IstL@09#1043 build/libs/ROOT.war root@192.168.10.167:/root/grs/apache-tomcat-8.5.34/webapps/

#call plink root@192.168.10.167 -pw IstL@09#1043 rm -rf /root/grs/apache-tomcat-8.5.34/webapps/ROOT

# call plink root@192.168.10.167 -pw IstL@09#1043 /root/grs/apache-tomcat-8.5.34/bin/startup.sh
# call plink root@192.168.10.167 -pw IstL@09#1043 tail -f /root/grs/apache-tomcat-8.5.34/logs/catalina.out



call pscp -P 22 -pw IstL@09#1043 build/libs/grs.war root@192.168.10.167:/root/grs/warRun/
# call plink root@192.168.10.167 -pw IstL@09#1043 screen -S grsWar
# call plink root@192.168.10.167 -pw IstL@09#1043 screen -dr grsWar | ./root/grs/warRun/start.sh

# call plink root@192.168.10.167 -pw IstL@09#1043 screen -ls | grep grsWar | cut -d. -f1 | awk '{print $1}' | xargs kill

# call plink root@192.168.10.167 -pw IstL@09#1043 cd /root/grs/| tail -f apache-tomcat-8.5.34/logs/catalina.out
