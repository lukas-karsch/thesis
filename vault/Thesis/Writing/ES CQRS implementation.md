First, look at the code structure / architecture and point out differences to the layered CRUD architecture (which is not so strong in my case, because CRUD also has vertical slices).
Show that a more descriptive architecture (by correctly separating read and command side) wouldve been even better.

The "api" package includes shared classes used to interface with the command and read side. They are the only publicly usable classes. 

Show how aggregates represent the command side. Explain set-based validation and how it's implemented. Show EnrollmentCommandHandler: handle commands which include several aggregates outside of the aggregates. Explain Sagas as process managers using AwardCreditsSaga. 

Show how projectors represent the read side. How an event arrives, it gets handled and transformed into the presentation that will later be returned. 

Explain the problem of synchronous responses (for error cases), e.g. enrolling (which expects a "ENROLLED" or "WAITLISTED" response) and how it was solved using subscription queries. Explain that this is a downside due to same contract tests, and typically, it is solved differently. Justify this in the context of the thesis. 

Explain that command side classes are package private so they can not be accessed from anywhere else. Same goes for the read side. The public app API (controllers) never access any of the classes, only API classes. 

Then, show one trace of a command request through the system (controller -> command bus -> command handler -> event emitted -> event sourcing handlers -> read side event handlers). Illustrate this using a diagram. 
Afterwards, show how a read request passes to the read side where denormalized data is stored and returned directly. 
## Full text 
