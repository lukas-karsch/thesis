I tried running k6 test `enroll-to-lecture.js`
when debugging another issue, i found out that the CollectionWithIdSerializer is insanely slow 
N+1 query is not at all avoided by my custom serialization approach

The bug in `enroll-to-lecture.js` must also still be solved 
## Solution
The only solution seems to be to switch to Envers. I will do that now, every entity is @Audited 
## Result
Worked like a charm. Added `@Audited` to all my entities. Used the opportunity to move createdAt and updatedAt fields to the AuditableEntity. Also the lastModifiedBy, by adding custom AuditorAware bean. 

Made sure Envers and JPA know about my custom system time. 

All tests on impl-crud pass. Only took 60 minutes... Shouldve done that way earlier, would have saved hours of work spent implementing a terrible custom audit log 

After making this change, the CRUD application sped up immensely in the "read lectures" test
