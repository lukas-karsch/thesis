#DDD
## Intro
- The domain model is our internal representation of the target domain (p. 5)
- We can and we should create a language to communicate specific issues about the domain (p.6)
- domain experts are not software experts! (p.8)
- the domain model can, and will differ from how real world experts see the domain
## Ubiquitous language 
The problem: developers talk in their own language - code, classes, OOP, paradigms, algorithms. The domain experts do
not understand any of this! At the same time, they _also_ have their own language. An air traffic controller talks about
planes, routes, deviations, etc. (p. 13f)

> Therefore, it is necessary to agree on a shared language that is used to describe the domain during communication
> between experts - the ubiquitous language!

- language is based on the model (p. 15)
- called "Ubiquitous Language" because it appears EVERYWHERE in communication - written, spoken, in diagrams and
  documents (p. 15)
- All parts of the code have to follow this language too: method names, classes, etc
- Nouns of the ubiquitous language = objects; verbs = their behavior (p. 37)
### UML (p. 20)
#uml
What else is needed to build a model? Is UML not enough to build a model?

- UML is only good for a small amount of elements
- Good at classes and associations
- NOT good at expressing constraints and behavior
- UML can NOT express the _meaning_ the classes represent and not what the objects are supposed to do
### Documents (p. 21)
Use documents with drawings, language, diagrams to build the model
Use a collection of small documents rather than one large document that attempts to describe the entire model

## Model Driven Design (p. 23)
#model-driven-design
Next step: implement the model in code.
Difficult to accurately implement the model in code. Developers might change the model, add own ideas etc
As development continues, the divergence between original model and the software increases (p.23)

### Analysis Model (p.24)
#analysis-model
- seen as separate from code
- analysts build the model by sharing knowledge and trying to condense it into documents
- those documents are passed to developers later, they have to implement the software
- developers not involved in the analysis

> Problem: Analysts can not foresee future problems with their model; developers have to make changes that might violate
> the domain

> Analysis models are abandoned after development starts

### > Better approach (p. 25)
#oop
- closely relate domain modeling and software design -> include developers in the modeling process
- Goal: build a model that accurately represents the domain AND can be mapped to code
- Errors and defects in the model are spotted early
- Developers must know the model extremely well
- Start by designing the code extremely close to the domain, then revisit it later to BOTH implement it _more naturally_ in software AND _more accurately_ to the domain
- typically make use of OOP: well-suited to design models because it supports the same paradigms (p. 27)
- keep the model as simple as possible. drop associations that are not essential to the model (p. 43)
## Building blocks of model design
#diagram
See p. 28 of the book for great diagrams
### Layers (p. 30)
> **Why use layers?** mixing concerns makes code difficult to reason about, difficult to test, difficult to extend and refactor

> Therefore, partition complex programs into layers. Layers ONLY depend on layers below them.

> Use loose coupling

> Domain objects must have no responsibility about storage, display. They should _only_ express the domain

| Layer                | Task                                                                                                                      |
|----------------------|---------------------------------------------------------------------------------------------------------------------------|
| Presentation Layer   | Present information and handle commands                                                                                   |
| Application Layer    | Coordinate app activity. Do not hold business logic, but delegate tasks and hold information about their progress (p. 31) |
| Domain Layer         | Information about the domain. Stateful objects.                                                                           |
| Infrastructure Layer | Supports other layers. Handles communication, persistence                                                                 |
> Example about those layers on p. 31
### Entities
#entity #identity
> Objects with an identity -> UNIQUE attribute of an object
Identities (p. 32)
- a unique attribute
- a combination of attributes
- an attribute _specially_ created to keep objects unique
- behavior

Comparison always occurs on the identifier -> opposed to value objects!
**Examples:**
- Student
- Course
- A "Grade" might be a value object.[[#Value Objects (p. 34)]]

Identities come at a performance cost! Tracking identities is expensive and requires work
> Select entities thoughtfully, only when an identity is necessary to keep track of!
### Value Objects (p. 34)
#immutable
- only care about the _value_ (= attributes) of the object
    - e.g. a `Point` class
- have no identity -> easily created and discarded
- immutable
- shareable
- can be nested
    - fields that belong together conceptually should be grouped into a separate value object
- can contain references to entities (p. 36)
### Services (p. 37)
#service
- some (behavioral) aspects of the domain are not easily mapped to objects / do not belong to objects
    - Example: transfering money between accounts
- That kind of behavior belongs inside a Service
- Services encapsulate concepts
- Services act as interfaces which provide operations (p. 38)
- Services are a point of connection for many objects
- adding service methods to domain objects leads to tight coupling between them = poor design & maintainability
- Services can be inside of application, domain or infrastructure layer - be careful!
#### Service characteristics
1. The operation performed by the Service refers to a domain concept which does not naturally belong to an Entity or value Object.
2. The operation performed refers to other objects in the domain.
3. The operation is stateless.

> [!WARNING] Careful
> Keep the domain layer isolated! Do not mix its services' responsibilities with those of services from the application or infrastructure layer!

Practical example on services, see p. 39
### Modules (p. 40)
#module
- as an application grows, the model grows
- model becomes hard to understand as a whole
- modules group related concepts
- reduce complexity and coupling
- modules should expose clear interfaces with well defined tasks
- the model should be partitioned into high-level domain concepts which correspond to modules
### Aggregates (p. 42)
#aggregate
- Large models contain many domain objects that are associated
- web of dependencies
- complex to manage lifecycles
- difficult to make sure all references to objects are dropped when there are many relationships in the model
- difficult to keep integrity and guarantee proper updates, when too many references exist
- difficult to enfore invariants (= illegal states)
  **The solution: Aggregates** (p. 44)
- Aggregate = one group of associated objects
- considered as one unit when changing data
- _One_ aggregate root
- Root is an entity ([[#Entities]])
- Root holds references to other entities and value objects
- Only the root is accessible from the outside, other objects have to be accessed via association traversal
- Aggregates _have global identity_
- Changes to the aggregate only via the root -> easy to enforce invariants
- Root passes transient references to objects inside the aggregate (pass copies of objects)
- Only the aggregate root is accessible through queries to the database
### Factories (p. 46)
#factory
- Entities and aggregates are complex to construct -> too complex for constructors
- When an object has complex structure / rules, a client that wants to construct them has to have internal knowledge ->
  breaks encapsulation!
  Therefore, use factories!
- Factories encapsulate the knowledge on object creation
- Especially useful to create aggregates ([[#Aggregates (p. 42)]])
- Creation process must be atomic
- Raise an exception if an object can not be fully created / invariants are violated
- Factories are part of the domain
  References [[Design Patterns by Gamma et all]]

> Factories are Domain Only. Factories create domain objects from scratch.

### Repositories (p. 51)
#repository
> Factories mix domain and infrastructure code. Factories reconstitute objects that previously existed (p. 55)

- repositories find already existing objects (by criteria) and store objects
- repositories abstract the underlying storage and create the _illusion_ that all objects just exist in memory and can
  easily be accessed
- when a new object is added to the repository, first create it with a factory, then pass to the
  repository ([[#Factories (p. 46)]])
- repositories get the data from underlying storage and create the domain objects using constructor or factories
- create repositories only for aggregate roots
  Goal: the domain and clients should NOT directly interact with the infrastructure, but stay focused on the domain (p.
  52)
