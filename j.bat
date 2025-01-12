rem Compile all java files and run the class specified as parameter 1
set j=C:\Users\Phill\java\jdk-23.0.1\bin
%j%\javac -d  Classes *.java
%j%\java  -cp Classes  com.AppaApps.Silicon.%1
