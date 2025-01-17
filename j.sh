echo $1
javac -d  Classes *.java
java  -cp Classes  com.AppaApps.Silicon.$1
