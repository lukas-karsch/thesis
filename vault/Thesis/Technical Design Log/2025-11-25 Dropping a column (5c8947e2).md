#schema-drift
I just dropped the "courseAssessments" from `Course`. I felt like it didn't make sense (in the 
domain), because the assessments should be decided by the professor who holds a lecture. 
I always recreate my database, so I will not have any problems with the migration, but it reminded me of a topic that I thought of while writing my exposé: schema drift. 

It might be interesting to see how the audit log could handle column changes! But that probably also be a problem with the DDD / ES approach, and it's a whole new topic to explore.
So probably, NON GOAL