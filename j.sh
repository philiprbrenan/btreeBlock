j="${1%.java}"
javac -d  Classes *.java
if [ $? -ne 0 ]; then
  echo "Java syntax errors"
  exit 1
fi
if [ -n "$j" ]; then
  java  -cp Classes  com.AppaApps.Silicon.$j 
fi
