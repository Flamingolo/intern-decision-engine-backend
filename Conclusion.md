# Conclusion

## Strengths


### Input validation
- The input validation is very strong. It checks for the correct number of arguments, the correct type of arguments, and the correct range of arguments.

### Calculations
- The credit modifier is calculated correctly based on the last 4 digits of the personal code.
- It calculates the valid loan amount based on the credit modifier and adjusts it based on the minimum threshold.

### Documentation
- The code is well documented. It explains what each function does and what the input and output of each function is.


## Weaknesses


### Lack of credit score calculation
- There is no implementation of credit score calculation. A critical part of the process is missing.

### Lack of flexibility
- It adjusts the loan only when loan amount is below minimum threshold. It doesn't take into account credit score or other factors that could affect the loan amount.




## Notes
- Validation inside request object
- Error included in response object
- Error status code is 400, maybe it should be custom status code or something else

