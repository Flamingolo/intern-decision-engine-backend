# Conclusion for Ticket-101

## Overview

### Strengths:

- For MVP the project is well implemented but missing critical logic
- Using Unit tests
- Well documented



### Areas for improvement:
- Missing critical logic for MVP
- BigDecimal is not used for currency calculations
- If in the beginning Long is used for amount, it should stay Long
- The more I started calculating expected outcomes, the less the original implementation worked
- Documentation is well done, but it shouldn't be necessary to understand the code
- Variables, constants and so on should be named more descriptively
- Use of arbitrary numbers in the code (made sense because of the documentation, but shouldn't be required to read method documentation to understand the code)
- The code is not very readable



### SOLID principles:
- A class should have one and only one reason to change, meaning that a class should have only one job.
- Objects or entities should be open for extension but closed for modification.
- Let q(x) be a property provable about objects of x of type T. Then q(y) should be provable for objects y of type S where S is a subtype of T.
- A client should never be forced to implement an interface that it doesn’t use, or clients shouldn’t be forced to depend on methods they do not use.
- Entities must depend on abstractions, not on concretions. It states that the high-level module must not depend on the low-level module, but they should depend on abstractions.
