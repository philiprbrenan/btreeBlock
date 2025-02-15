reg [4 : 0] opCodeMap[11 : 0];
task initialize_opCodeMap;
  begin
        opCodeMap[0] <= 0;
        opCodeMap[1] <= 1;
        opCodeMap[2] <= 2;
        opCodeMap[9] <= 2;
        opCodeMap[3] <= 3;
        opCodeMap[4] <= 4;
        opCodeMap[5] <= 5;
        opCodeMap[6] <= 6;
        opCodeMap[7] <= 7;
        opCodeMap[8] <= 8;
        opCodeMap[10] <= 9;
        opCodeMap[11] <= 10;
  end
endtask
