call "C:/Program Files/gradle-3.4.1/bin/gradle" clean
call "C:/Program Files/gradle-3.4.1/bin/gradle" build
call pscp -P 22 -pw @Dw19RcmS2020$f build/libs/ROOT.war admin@115.127.88.53:/home/admin/t/
