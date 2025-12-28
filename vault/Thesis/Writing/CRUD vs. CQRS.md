Distinction between CQRS and CRUD 

| CQRS                         | CRUD (Opposite)                |
| ---------------------------- | ------------------------------ |
| Separate read & write models | Unified read/write model       |
| Optimized independently      | One-size-fits-all              |
| Commands don’t return data   | Commands often return entities |
| Often event-driven           | Usually state-driven           |
| Higher complexity            | Lower complexity               |
If CQRS says:
> “Reads and writes have fundamentally different concerns”

CRUD says:
> “Reads and writes are just operations on the same data”

- CQRS has built-in auditability 