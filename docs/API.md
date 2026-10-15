# Spring PetClinic REST API

The versioned JSON API is available under `/api/v1`. The existing Thymeleaf UI and
its URLs are unchanged. Interactive documentation is at `/swagger-ui.html`, and the
OpenAPI document is at `/v3/api-docs`.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET, POST | `/api/v1/owners` | Search/page owners; create an owner |
| GET, PUT, DELETE | `/api/v1/owners/{ownerId}` | Read, replace, or delete an owner |
| GET, POST | `/api/v1/owners/{ownerId}/pets` | List or create an owner's pets |
| GET, PUT | `/api/v1/pets/{petId}` | Read or replace a pet |
| GET, POST | `/api/v1/pets/{petId}/visits` | List or create visits |
| GET | `/api/v1/vets` | Page veterinarians |
| GET | `/api/v1/vets/{vetId}` | Read a veterinarian |
| GET | `/api/v1/pet-types` | List valid pet types |

Owner and veterinarian lists accept zero-based `page`, `size` (maximum 100), and
repeatable `sort=property,direction` parameters. Owners also accept a
`lastName` prefix filter.

```bash
curl 'http://localhost:18089/api/v1/owners?lastName=F&page=0&size=10&sort=lastName,asc'

curl -i -X POST http://localhost:18089/api/v1/owners \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Jamie","lastName":"Lee","address":"10 Main St","city":"Madison","telephone":"6085550100"}'

curl -i -X POST http://localhost:18089/api/v1/owners/1/pets \
  -H 'Content-Type: application/json' \
  -d '{"name":"Milo","birthDate":"2022-05-10","typeId":1}'

curl -i -X POST http://localhost:18089/api/v1/pets/1/visits \
  -H 'Content-Type: application/json' \
  -d '{"date":"2026-10-20","description":"Annual checkup"}'
```

Creates return `201 Created` with a `Location` header, owner deletion returns
`204 No Content`, and missing resources return `404 Not Found`. Validation errors
use RFC 7807 `application/problem+json` and include a `fieldErrors` object:

```json
{
  "type": "urn:petclinic:problem:400",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/owners",
  "fieldErrors": { "telephone": "must contain exactly 10 digits" }
}
```

Every response includes `X-Correlation-Id`; an incoming safe value is retained.
API requests also receive `X-RateLimit-Limit`, `X-RateLimit-Remaining`, and
`X-RateLimit-Reset`. Configure the limiter with:

| Property | Default | Meaning |
| --- | --- | --- |
| `petclinic.ratelimit.enabled` | `true` | Enable rate limiting for `/api/**` |
| `petclinic.ratelimit.requests-per-window` | `60` | Requests allowed per client IP |
| `petclinic.ratelimit.window` | `1m` | Fixed-window duration |
| `petclinic.ratelimit.max-clients` | `10000` | Maximum in-memory client buckets |

Health probes are available at `/actuator/health/liveness` and
`/actuator/health/readiness`; metrics are at `/actuator/metrics`, and Prometheus
scraping is at `/actuator/prometheus`.

## Docker Compose

Run the application and PostgreSQL (the host port intentionally avoids 8080):

```bash
docker compose up --build
```

The UI is then at `http://localhost:18089`. Override the host port with
`PETCLINIC_PORT`, for example `PETCLINIC_PORT=18090 docker compose up --build`.
