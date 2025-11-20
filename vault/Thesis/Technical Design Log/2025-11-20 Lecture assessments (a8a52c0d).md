Course assessments are value objects.
Right now, I am adding the "lecture assessment" and "grade" entities, and im debating if they should have a relationship to course assessments.
If yes, that would mean the course assessments had to be entities. 
## Advantages
- can create a lecture assessment _from_ a course assessment 
- enforce that lecture assessments match course assessments 
## Disadvantages
- added complexity
