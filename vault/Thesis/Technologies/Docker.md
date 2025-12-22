Docker can be used to containerize my Spring applications.
## Commands 
Build the image:
**CRUD** 
```bash
docker build -t lkarsch/impl-crud-docker .
```

**ES-CQRS**
```bash
docker build -t lkarsch/impl-es-cqrs-docker .
```

**Run with docker-compose**
```bash
docker-compose up --build -d crud-app
```
