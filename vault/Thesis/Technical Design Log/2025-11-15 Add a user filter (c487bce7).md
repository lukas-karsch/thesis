[[(How To Fake) Authentication]]
I implemented the user filter: `karsch.lukas.UserFilter` 

It checks the `customAuth` header for a string of this format: 
- "professor_1"
- "student_1"
And adds the user type and user id to a scoped `RequestContext` object. 