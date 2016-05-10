setSingleled:
ajw         -18
ldc         0
stl         17
ldlp        17
ldc         @led0
ldc         4
out         
ldc         0
stl         16
ldlp        16
ldc         @led1
ldc         4
out         
ldc         0
stl         15
ldlp        15
ldc         @led2
ldc         4
out         
ldc         0
stl         14
ldlp        14
ldc         @led3
ldc         4
out         
ldc         0
stl         13
ldlp        13
ldc         @led4
ldc         4
out         
ldc         0
stl         12
ldlp        12
ldc         @led5
ldc         4
out         
ldc         0
stl         11
ldlp        11
ldc         @led6
ldc         4
out         
ldc         0
stl         10
ldlp        10
ldc         @led7
ldc         4
out         
ldl         20
ldc         0
diff        
eqc         0
eqc         0
cj          setSingleled~IL_143-$0
ldl         20
ldc         1
diff        
eqc         0
eqc         0
cj          setSingleled~IL_135-$0
ldl         20
ldc         2
diff        
eqc         0
eqc         0
cj          setSingleled~IL_127-$0
ldl         20
ldc         3
diff        
eqc         0
eqc         0
cj          setSingleled~IL_119-$0
ldl         20
ldc         4
diff        
eqc         0
eqc         0
cj          setSingleled~IL_111-$0
ldl         20
ldc         5
diff        
eqc         0
eqc         0
cj          setSingleled~IL_103-$0
ldl         20
ldc         6
diff        
eqc         0
eqc         0
cj          setSingleled~IL_95-$0
ldl         20
ldc         7
diff        
eqc         0
eqc         0
cj          setSingleled~IL_87-$0
ldc         1
eqc         0
cj          setSingleled~IL_84-$0
setSingleled~IL_84:
L1:         
diff        
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_87:
L2:         
diff        
ldl         21
stl         9
ldlp        9
ldc         @led7
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_95:
L3:         
diff        
ldl         21
stl         8
ldlp        8
ldc         @led6
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_103:
L4:         
diff        
ldl         21
stl         7
ldlp        7
ldc         @led5
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_111:
L5:         
diff        
ldl         21
stl         6
ldlp        6
ldc         @led4
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_119:
L6:         
diff        
ldl         21
stl         5
ldlp        5
ldc         @led3
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_127:
L7:         
diff        
ldl         21
stl         4
ldlp        4
ldc         @led2
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_135:
L8:         
diff        
ldl         21
stl         3
ldlp        3
ldc         @led1
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_143:
L9:         
diff        
ldl         21
stl         2
ldlp        2
ldc         @led0
ldc         4
out         
ldc         0
cj          setSingleled~IL_151-$0
setSingleled~IL_151:
L0:         
diff        
ajw         18
ret         

init:       
ajw         -7
ldc         1
stl         6
ldc         0
stl         5
ldc         0
cj          init~IL_31-$0
ldc         0
cj          init~IL_7-$0
init~IL_7:  
L11:        
diff        
ldl         6
ldl         5
add         
stl         4
ldl         4
ldc         8
rem         
stl         3
ldc         1
ldl         3
ldc         0
ajw         2
call        setSingleled-$0
ajw         -2
ldl         6
stl         5
ldl         4
stl         6
ldtimer     
stl         2
ldl         2
ldc         5000000
sum         
stl         2
ldl         2
tin         
init~IL_31: 
L10:        
diff        
ldc         1
eqc         0
cj          init~IL_7-$0
ajw         7
ret         


