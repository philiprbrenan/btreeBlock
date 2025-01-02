reg [2:0] M_base;
reg [7:0] M;
task initialize_memory_M;
    begin
        M[0] = 0;
        M[1] = 0;
        M[2] = 0;
        M[3] = 0;
        M[4] = 1;
        M[5] = 1;
        M[6] = 1;
        M[7] = 1;
    end
endtask
