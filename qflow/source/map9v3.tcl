# IRSIM simulation for map9v3

l gnd
h vdd

vector N N\[8:0\]
vector dp dp\[8:0\]
vector sr sr\[7:0\]
vector counter counter\[7:0\]

analyzer

ana clock start reset N dp done counter sr

setvector N 0d220
l clock
l start
h reset

s 500
l reset
s 500
h start
s 500
l start
s 500

every 100 {toggle clock}

s 50000

