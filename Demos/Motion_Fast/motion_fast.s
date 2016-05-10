init:       
ajw         -29
ldc         0
stl         28
ldc         1
stl         27
ldc         0
stl         25
ldlp        25
ldc         @led0
ldc         4
out         
ldc         1
stl         24
ldlp        24
ldc         @led1
ldc         4
out         
ldc         1
stl         23
ldlp        23
ldc         @led2
ldc         4
out         
ldc         1
stl         22
ldlp        22
ldc         @led3
ldc         4
out         
ldc         1
stl         21
ldlp        21
ldc         @led4
ldc         4
out         
ldc         1
stl         20
ldlp        20
ldc         @led5
ldc         4
out         
ldc         1
stl         19
ldlp        19
ldc         @led6
ldc         4
out         
ldc         1
stl         18
ldlp        18
ldc         @led7
ldc         4
out         
ldc         0
cj          init~IL_348-$0
ldc         0
cj          init~IL_55-$0
init~IL_55: 
L1:         
diff        
ldlp        28
ldc         @sensor
ldc         4
in          
ldl         28
ldc         0
diff        
eqc         0
eqc         0
cj          init~IL_304-$0
ldl         27
ldc         0
diff        
eqc         0
eqc         0
cj          init~IL_278-$0
ldl         27
ldc         1
diff        
eqc         0
eqc         0
cj          init~IL_252-$0
ldl         27
ldc         2
diff        
eqc         0
eqc         0
cj          init~IL_226-$0
ldl         27
ldc         3
diff        
eqc         0
eqc         0
cj          init~IL_200-$0
ldl         27
ldc         4
diff        
eqc         0
eqc         0
cj          init~IL_174-$0
ldl         27
ldc         5
diff        
eqc         0
eqc         0
cj          init~IL_148-$0
ldl         27
ldc         6
diff        
eqc         0
eqc         0
cj          init~IL_122-$0
ldl         27
ldc         7
diff        
eqc         0
eqc         0
cj          init~IL_96-$0
init~IL_96: 
L3:         
diff        
ldc         0
stl         17
ldlp        17
ldc         @led7
ldc         4
out         
ldc         1
stl         16
ldlp        16
ldc         @led6
ldc         4
out         
ldc         0
stl         17
ldlp        17
ldc         @led7
ldc         4
out         
ldc         1
stl         16
ldlp        16
ldc         @led6
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_122:
L4:         
diff        
ldc         0
stl         15
ldlp        15
ldc         @led6
ldc         4
out         
ldc         1
stl         14
ldlp        14
ldc         @led5
ldc         4
out         
ldc         0
stl         15
ldlp        15
ldc         @led6
ldc         4
out         
ldc         1
stl         14
ldlp        14
ldc         @led5
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_148:
L5:         
diff        
ldc         0
stl         13
ldlp        13
ldc         @led5
ldc         4
out         
ldc         1
stl         12
ldlp        12
ldc         @led4
ldc         4
out         
ldc         0
stl         13
ldlp        13
ldc         @led5
ldc         4
out         
ldc         1
stl         12
ldlp        12
ldc         @led4
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_174:
L6:         
diff        
ldc         0
stl         11
ldlp        11
ldc         @led4
ldc         4
out         
ldc         1
stl         10
ldlp        10
ldc         @led3
ldc         4
out         
ldc         0
stl         11
ldlp        11
ldc         @led4
ldc         4
out         
ldc         1
stl         10
ldlp        10
ldc         @led3
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_200:
L7:         
diff        
ldc         0
stl         9
ldlp        9
ldc         @led3
ldc         4
out         
ldc         1
stl         8
ldlp        8
ldc         @led2
ldc         4
out         
ldc         0
stl         9
ldlp        9
ldc         @led3
ldc         4
out         
ldc         1
stl         8
ldlp        8
ldc         @led2
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_226:
L8:         
diff        
ldc         0
stl         7
ldlp        7
ldc         @led2
ldc         4
out         
ldc         1
stl         6
ldlp        6
ldc         @led1
ldc         4
out         
ldc         0
stl         7
ldlp        7
ldc         @led2
ldc         4
out         
ldc         1
stl         6
ldlp        6
ldc         @led1
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_252:
L9:         
diff        
ldc         0
stl         5
ldlp        5
ldc         @led1
ldc         4
out         
ldc         1
stl         4
ldlp        4
ldc         @led0
ldc         4
out         
ldc         0
stl         5
ldlp        5
ldc         @led1
ldc         4
out         
ldc         1
stl         4
ldlp        4
ldc         @led0
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_278:
L10:        
diff        
ldc         0
stl         3
ldlp        3
ldc         @led0
ldc         4
out         
ldc         1
stl         2
ldlp        2
ldc         @led7
ldc         4
out         
ldc         0
stl         3
ldlp        3
ldc         @led0
ldc         4
out         
ldc         1
stl         2
ldlp        2
ldc         @led7
ldc         4
out         
ldc         0
cj          init~IL_308-$0
init~IL_304:
L11:        
diff        
ldc         0
cj          init~IL_308-$0
init~IL_308:
L2:         
diff        
ldl         28
ldc         0
diff        
eqc         0
eqc         0
cj          init~IL_335-$0
ldl         27
ldc         7
diff        
eqc         0
eqc         0
cj          init~IL_329-$0
ldc         1
eqc         0
cj          init~IL_319-$0
init~IL_319:
L13:        
diff        
ldl         27
ldc         1
add         
stl         27
ldl         27
ldc         1
add         
stl         27
ldc         0
cj          init~IL_339-$0
init~IL_329:
L14:        
diff        
ldc         0
stl         27
ldc         0
stl         27
ldc         0
cj          init~IL_339-$0
init~IL_335:
L15:        
diff        
ldc         0
cj          init~IL_339-$0
init~IL_339:
L12:        
diff        
ldtimer     
stl         26
ldl         26
ldc         100000
sum         
stl         26
ldl         26
tin         
init~IL_348:
L0:         
diff        
ldc         1
eqc         0
cj          init~IL_55-$0
ajw         29
ret         


