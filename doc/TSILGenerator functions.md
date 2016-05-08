## List of TSILGenerator functions

*Ticked indicates function has been created but not necessarily completed.*

* [ ] `visitAbbreviation_expression`
* [ ] `visitAbbreviation_name_channel`
* [ ] `visitAbbreviation_name_channel_list`
* [ ] `visitAbbreviation_name_port`
* [ ] `visitAbbreviation_name_timer`
* [ ] `visitAbbreviation_name_var`
* [ ] `visitAbbreviation_spec_channel`
* [ ] `visitAbbreviation_spec_channel_list`
* [ ] `visitAbbreviation_spec_expression`
* [ ] `visitAbbreviation_spec_port`
* [ ] `visitAbbreviation_spec_timer`
* [ ] `visitAbbreviation_spec_var`
* [ ] `visitActual_channel`
* [ ] `visitActual_expression`
* [ ] `visitActual_port`
* [ ] `visitActual_timer`
* [ ] `visitActual_variable`
* [ ] `visitAllocation`
* [ ] `visitAlternation_alternatives`
* [ ] `visitAlternation_replicator_alternative`
* [ ] `visitAlternative_alternation`
* [ ] `visitAlternative_bool_channel`
* [ ] `visitAlternative_channel`
* [ ] `visitAlternative_guarded`
* [ ] `visitAlternative_spec`
* [x] `visitAssignment`
* [x] `visitBase`
* [x] `visitBool`
* [ ] `visitCase_expression`
* [ ] `visitCase_input`
* [ ] `visitChannel_channel_expression`
* [ ] `visitChannel_expression_channel_type`
* [ ] `visitChannel_for_count`
* [ ] `visitChannel_from_base`
* [ ] `visitChannel_name`
* [ ] `visitChannel_type_protocol`
* [x] `visitChoice_conditional`
* [x] `visitChoice_guarded`
* [ ] `visitChoice_specification`
* [ ] `visitCompound_stmt_alternation`
* [ ] `visitCompound_stmt_case_input`
* [ ] `visitCompound_stmt_conditional`
* [ ] `visitCompound_stmt_loop`
* [ ] `visitCompound_stmt_parallel`
* [ ] `visitCompound_stmt_selection`
* [ ] `visitCompound_stmt_sequence`
* [ ] `visitCompound_stmt_spec_or_alloc_stmt`
* [x] `visitConditional_choices`
* [ ] `visitConditional_replicator`
* [ ] `visitConversion`
* [x] `visitCount`
* [x] `visitData_type_bool`
* [x] `visitData_type_byte`
* [x] `visitData_type_expr_data_type`
* [x] `visitData_type_int`
* [x] `visitData_type_int16`
* [x] `visitData_type_int32`
* [x] `visitData_type_int64`
* [x] `visitData_type_name`
* [x] `visitData_type_real32`
* [x] `visitData_type_real64`
* [x] `visitDeclaration`
* [ ] `visitDef_DATA_Name`
* [x] `visitDef_function_expression_list`
* [x] `visitDef_function_value_process`
* [x] `visitDef_PROC`
* [ ] `visitDef_PROTOCOL_NAME_INDENT`
* [ ] `visitDef_PROTOCOL_NAME_IS`
* [ ] `visitDef_specifier`
* [ ] `visitDef_specifier2`
* [ ] `visitDef_val`
* [ ] `visitDelayed_input`
* [x] `visitDyadic_operator`
* [ ] `visitExpression_conversion`
* [x] `visitExpression_dyadic_operator`
* [x] `visitExpression_list_expressions`
* [x] `visitExpression_list_function_call`
* [x] `visitExpression_monadic`
* [ ] `visitExpression_most_data_type`
* [x] `visitExpression_operand`
* [ ] `visitExpression_size_of`
* [ ] `visitField_name`
* [x] `visitFile_input`
* [x] `visitFormal`
* [x] `visitFunction_call`
* [x] `visitFunction_header`
* [ ] `visitGuarded_alternative`
* [x] `visitGuarded_choice`
* [ ] `visitGuard_bool_input_or_skip`
* [ ] `visitGuard_input`
* [ ] `visitInput_channel_input_items`
* [ ] `visitInput_channel_tagged_list`
* [ ] `visitInput_delayed_input`
* [ ] `visitInput_item_multiple_variables`
* [ ] `visitInput_item_variable`
* [ ] `visitInput_port_variable`
* [ ] `visitInput_timer_input`
* [x] `visitLiteral_integer`
* [x] `visitLiteral_byte`
* [ ] `visitLiteral_real`
* [x] `visitLiteral_true`
* [x] `visitLiteral_false`
* [x] `visitLoop`
* [x] `visitMonadic_operator`
* [ ] `visitOperand_bytesin`
* [x] `visitOperand_expression`
* [x] `visitOperand_literal`
* [x] `visitOperand_function_call`
* [ ] `visitOperand_offsetof`
* [ ] `visitOperand_operand_expression`
* [ ] `visitOperand_table`
* [ ] `visitOperand_value_process`
* [x] `visitOperand_variable`
* [ ] `visitOption_case_expression_stmt`
* [ ] `visitOption_spec_option`
* [ ] `visitOption_stmt`
* [ ] `visitOutputitem_multiple_expression`
* [ ] `visitOutputitem_single_expression`
* [ ] `visitOutput_channel_outputitems`
* [ ] `visitOutput_channel_tag_outputitems`
* [ ] `visitOutput_port_expression`
* [ ] `visitParallel_placedpar`
* [x] `visitParallel_pripar_replicator`
* [x] `visitParallel_pripar_suite`
* [ ] `visitPlacedpar_expression_stmt`
* [ ] `visitPlacedpar_placedpars`
* [ ] `visitPlacedpar_replicator_placedpar`
* [ ] `visitPort_name`
* [ ] `visitPort_port_base_count`
* [ ] `visitPort_port_count`
* [ ] `visitPort_port_expression`
* [ ] `visitPort_type_data_type`
* [ ] `visitPort_type_expression_port_type`
* [ ] `visitProc_instance`
* [ ] `visitProtocol`
* [x] `visitReplicator`
* [ ] `visitSelection`
* [ ] `visitSelector`
* [ ] `visitSequence_replicator`
* [x] `visitSequence_suite`
* [ ] `visitSequential_protocol`
* [ ] `visitSimple_protocol`
* [x] `visitSimple_stmt`
* [x] `visitSmall_stmt_assignment`
* [ ] `visitSmall_stmt_input`
* [ ] `visitSmall_stmt_output`
* [ ] `visitSmall_stmt_proc`
* [x] `visitSmall_stmt_skip`
* [ ] `visitSmall_stmt_stop`
* [x] `visitSpecificationAbrv`
* [x] `visitSpecificationDec`
* [x] `visitSpecificationDef`
* [ ] `visitSpecifier_channel_type`
* [x] `visitSpecifier_data_type`
* [ ] `visitSpecifier_expression_specifier`
* [ ] `visitSpecifier_port_type`
* [ ] `visitSpecifier_timer_type`
* [x] `visitStmt_compound_stmt`
* [x] `visitStmt_simple_stmt`
* [ ] `visitStructured_type`
* [x] `visitSuite`
* [ ] `visitTable_expressions`
* [ ] `visitTable_string`
* [ ] `visitTable_table_base_count`
* [ ] `visitTable_table_count`
* [ ] `visitTable_table_expression`
* [ ] `visitTag`
* [ ] `visitTagged_list`
* [ ] `visitTagged_protocol`
* [ ] `visitTimer_expression`
* [ ] `visitTimer_expression_timer_type`
* [ ] `visitTimer_input`
* [ ] `visitTimer_name`
* [ ] `visitTimer_timer_base_count`
* [ ] `visitTimer_timer_count`
* [ ] `visitTimer_type_timer`
* [x] `visitValue_process_specification`
* [x] `visitValue_process_stmt`
* [x] `visitVariable_list`
* [x] `visitVariable_name`
* [ ] `visitVariable_variable_base_count`
* [ ] `visitVariable_variable_count`
* [ ] `visitVariable_variable_expression`
* [ ] `visitVariant_specification_variant`
* [ ] `visitVariant_tagged_list_stmt`

## Work Assignment

|                                           |   Ed  |  Ross |  John |  Sam  |
|-------------------------------------------|:-----:|:-----:|:-----:|:-----:|
| `visitAbbreviation_expression`            |   -   |       |       |       |
| `visitAbbreviation_name_channel`          |   -   |       |       |       |
| `visitAbbreviation_name_channel_list`     |   -   |       |       |       |
| `visitAbbreviation_name_port`             |   -   |       |       |       |
| `visitAbbreviation_name_timer`            |   -   |       |       |       |
| `visitAbbreviation_name_var`              |   -   |       |       |       |
| `visitAbbreviation_spec_channel`          |   -   |       |       |       |
| `visitAbbreviation_spec_channel_list`     |   -   |       |       |       |
| `visitAbbreviation_spec_expression`       |   -   |       |       |       |
| `visitAbbreviation_spec_port`             |   -   |       |       |       |
| `visitAbbreviation_spec_timer`            |   -   |       |       |       |
| `visitAbbreviation_spec_var`              |   -   |       |       |       |
| `visitActual_channel`                     |   -   |       |       |       |
| `visitActual_expression`                  |   -   |       |       |       |
| `visitActual_port`                        |   -   |       |       |       |
| `visitActual_timer`                       |   -   |       |       |       |
| `visitActual_variable`                    |   -   |       |       |       |
| `visitAllocation`                         |       |       |   -   |       |
| `visitAlternation_alternatives`           |       |   -   |       |       |
| `visitAlternation_replicator_alternative` |       |   -   |       |       |
| `visitAlternative_alternation`            |       |   -   |       |       |
| `visitAlternative_bool_channel`           |       |   -   |       |       |
| `visitAlternative_channel`                |       |   -   |       |       |
| `visitAlternative_guarded`                |       |   -   |       |       |
| `visitAlternative_spec`                   |       |   -   |       |       |
| `visitAssignment`                         |       |   -   |       |       |
| `visitBase`                               |   -   |       |       |       |
| `visitBool`                               |   -   |       |       |       |
| `visitCase_expression`                    |       |       |   -   |       |
| `visitCase_input`                         |       |       |   -   |       |
| `visitChannel_channel_expression`         |       |       |   -   |       |
| `visitChannel_expression_channel_type`    |       |       |   -   |       |
| `visitChannel_for_count`                  |       |       |   -   |       |
| `visitChannel_from_base`                  |       |       |   -   |       |
| `visitChannel_name`                       |       |       |   -   |       |
| `visitChannel_type_protocol`              |       |       |   -   |       |
| `visitChoice_conditional`                 |       |       |   -   |       |
| `visitChoice_guarded`                     |       |       |   -   |       |
| `visitChoice_specification`               |       |       |   -   |       |
| `visitCompound_stmt_alternation`          |       |   -   |       |       |
| `visitCompound_stmt_case_input`           |       |       |   -   |       |
| `visitCompound_stmt_conditional`          |   -   |       |       |       |
| `visitCompound_stmt_loop`                 |   -   |       |       |       |
| `visitCompound_stmt_parallel`             |   -   |       |       |       |
| `visitCompound_stmt_selection`            |   -   |       |       |       |
| `visitCompound_stmt_sequence`             |   -   |       |       |       |
| `visitCompound_stmt_spec_or_alloc_stmt`   |   -   |       |       |       |
| `visitConditional_choices`                |       |       |   -   |       |
| `visitConditional_replicator`             |       |       |   -   |       |
| `visitConversion`                         |       |       |   -   |       |
| `visitCount`                              |   -   |       |       |       |
| `visitData_type_bool`                     |   -   |       |       |       |
| `visitData_type_byte`                     |   -   |       |       |       |
| `visitData_type_expr_data_type`           |   -   |       |       |       |
| `visitData_type_int`                      |   -   |       |       |       |
| `visitData_type_int16`                    |   -   |       |       |       |
| `visitData_type_int32`                    |   -   |       |       |       |
| `visitData_type_int64`                    |   -   |       |       |       |
| `visitData_type_name`                     |   -   |       |       |       |
| `visitData_type_real32`                   |   -   |       |       |       |
| `visitData_type_real64`                   |   -   |       |       |       |
| `visitDeclaration`                        |   -   |       |       |       |
| `visitDef_DATA_Name`                      |   -   |       |       |       |
| `visitDef_data_type`                      |   -   |       |       |       |
| `visitDef_data_type2`                     |   -   |       |       |       |
| `visitDef_PROC`                           |       |   -   |       |       |
| `visitDef_PROTOCOL_NAME_INDENT`           |       |   -   |       |       |
| `visitDef_PROTOCOL_NAME_IS`               |       |   -   |       |       |
| `visitDef_specifier`                      |   -   |       |       |       |
| `visitDef_specifier2`                     |   -   |       |       |       |
| `visitDef_val`                            |       |       |   -   |       |
| `visitDelayed_input`                      |       |       |   -   |       |
| `visitDyadic_operator`                    |       |   -   |       |       |
| `visitExpression_conversion`              |       |   -   |       |       |
| `visitExpression_dyadic_operator`         |       |   -   |       |       |
| `visitExpression_list_expressions`        |   -   |       |       |       |
| `visitExpression_list_name`               |       |   -   |       |       |
| `visitExpression_monadic`                 |       |   -   |       |       |
| `visitExpression_most_data_type`          |       |   -   |       |       |
| `visitExpression_operand`                 |       |   -   |       |       |
| `visitExpression_size_of`                 |       |   -   |       |       |
| `visitField_name`                         |   -   |       |       |       |
| `visitFile_input`                         |   -   |       |       |       |
| `visitFormal`                             |   -   |       |       |       |
| `visitFunction_header`                    |   -   |       |       |       |
| `visitGuarded_alternative`                |       |       |   -   |       |
| `visitGuarded_choice`                     |       |       |   -   |       |
| `visitGuard_bool_input_or_skip`           |       |       |   -   |       |
| `visitGuard_input`                        |       |       |   -   |       |
| `visitInput_channel_input_items`          |       |       |   -   |       |
| `visitInput_channel_tagged_list`          |       |       |   -   |       |
| `visitInput_delayed_input`                |       |       |   -   |       |
| `visitInput_item_multiple_variables`      |       |       |   -   |       |
| `visitInput_item_variable`                |       |       |   -   |       |
| `visitInput_port_variable`                |       |       |   -   |       |
| `visitInput_timer_input`                  |       |       |   -   |       |
| `visitLiteral`                            |       |   -   |       |       |
| `visitLoop`                               |   -   |       |       |       |
| `visitMonadic_operator`                   |       |   -   |       |       |
| `visitOperand_bytesin`                    |       |   -   |       |       |
| `visitOperand_expression`                 |   -   |       |       |       |
| `visitOperand_literal`                    |   -   |       |       |       |
| `visitOperand_name_expressionlist`        |       |   -   |       |       |
| `visitOperand_offsetof`                   |       |   -   |       |       |
| `visitOperand_operand_expression`         |       |   -   |       |       |
| `visitOperand_table`                      |       |   -   |       |       |
| `visitOperand_value_process`              |       |   -   |       |       |
| `visitOperand_variable`                   |   -   |       |       |       |
| `visitOption_case_expression_stmt`        |       |   -   |       |       |
| `visitOption_spec_option`                 |       |   -   |       |       |
| `visitOption_stmt`                        |       |   -   |       |       |
| `visitOutputitem_multiple_expression`     |       |       |   -   |       |
| `visitOutputitem_single_expression`       |       |       |   -   |       |
| `visitOutput_channel_outputitems`         |       |       |   -   |       |
| `visitOutput_channel_tag_outputitems`     |       |       |   -   |       |
| `visitOutput_port_expression`             |       |       |   -   |       |
| `visitParallel_placedpar`                 |       |   -   |       |       |
| `visitParallel_pripar_replicator`         |   -   |       |       |       |
| `visitParallel_pripar_suite`              |   -   |       |       |       |
| `visitPlacedpar_expression_stmt`          |       |   -   |       |       |
| `visitPlacedpar_placedpars`               |       |   -   |       |       |
| `visitPlacedpar_replicator_placedpar`     |       |   -   |       |       |
| `visitPort_name`                          |       |       |   -   |       |
| `visitPort_port_base_count`               |       |       |   -   |       |
| `visitPort_port_count`                    |       |       |   -   |       |
| `visitPort_port_expression`               |       |       |   -   |       |
| `visitPort_type_data_type`                |       |       |   -   |       |
| `visitPort_type_expression_port_type`     |       |       |   -   |       |
| `visitProc_instance`                      |       |       |   -   |       |
| `visitProtocol`                           |       |   -   |       |       |
| `visitReplicator`                         |   -   |       |       |       |
| `visitSelection`                          |       |   -   |       |       |
| `visitSelector`                           |       |   -   |       |       |
| `visitSequence_replicator`                |       |   -   |       |       |
| `visitSequence_suite`                     |       |   -   |       |       |
| `visitSequential_protocol`                |       |   -   |       |       |
| `visitSimple_protocol`                    |       |   -   |       |       |
| `visitSimple_stmt`                        |   -   |       |       |       |
| `visitSmall_stmt_assignment`              |   -   |       |       |       |
| `visitSmall_stmt_input`                   |       |   -   |       |       |
| `visitSmall_stmt_output`                  |       |   -   |       |       |
| `visitSmall_stmt_proc`                    |       |       |       |       |
| `visitSmall_stmt_skip`                    |   -   |       |       |       |
| `visitSmall_stmt_stop`                    |       |       |   -   |       |
| `visitSpecificationAbrv`                  |   -   |       |       |       |
| `visitSpecificationDec`                   |       |   -   |       |       |
| `visitSpecificationDef`                   |       |   -   |       |       |
| `visitSpecifier_channel_type`             |   -   |       |       |       |
| `visitSpecifier_data_type`                |   -   |       |       |       |
| `visitSpecifier_expression_specifier`     |   -   |       |       |       |
| `visitSpecifier_port_type`                |   -   |       |       |       |
| `visitSpecifier_timer_type`               |   -   |       |       |       |
| `visitStmt_compound_stmt`                 |   -   |       |       |       |
| `visitStmt_simple_stmt`                   |   -   |       |       |       |
| `visitStructured_type`                    |   -   |       |       |       |
| `visitSuite`                              |   -   |       |       |       |
| `visitTable_expressions`                  |   -   |       |       |       |
| `visitTable_string`                       |   -   |       |       |       |
| `visitTable_table_base_count`             |   -   |       |       |       |
| `visitTable_table_count`                  |   -   |       |       |       |
| `visitTable_table_expression`             |   -   |       |       |       |
| `visitTag`                                |   -   |       |       |       |
| `visitTagged_list`                        |   -   |       |       |       |
| `visitTagged_protocol`                    |   -   |       |       |       |
| `visitTimer_expression`                   |       |       |   -   |       |
| `visitTimer_expression_timer_type`        |       |       |   -   |       |
| `visitTimer_input`                        |       |       |   -   |       |
| `visitTimer_name`                         |       |       |   -   |       |
| `visitTimer_timer_base_count`             |       |       |   -   |       |
| `visitTimer_timer_count`                  |       |       |   -   |       |
| `visitTimer_type_timer`                   |       |       |   -   |       |
| `visitValue_process_specification`        |   -   |       |       |       |
| `visitValue_process_stmt`                 |   -   |       |       |       |
| `visitVariable_list`                      |   -   |       |       |       |
| `visitVariable_name`                      |   -   |       |       |       |
| `visitVariable_variable_base_count`       |       |   -   |       |       |
| `visitVariable_variable_count`            |       |   -   |       |       |
| `visitVariable_variable_expression`       |       |   -   |       |       |
| `visitVariant_specification_variant`      |       |   -   |       |       |
| `visitVariant_tagged_list_stmt`           |       |   -   |       |       |
