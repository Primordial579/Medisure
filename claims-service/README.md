# MediSure Claims Service

The Claims Service owns end-to-end claim lifecycle APIs for MediSure.

## Features

- Create claims using patient data from Hospital Service and policy validation from Insurance Service
- Retrieve claim by ID
- Retrieve all claims
- Update final bill and process final claim decision

## API Endpoints

- `POST /api/claims`
- `GET /api/claims/{id}`
- `GET /api/claims`
- `PUT /api/claims/{id}/final-bill`

Compatibility aliases (for earlier hospital claim route usage):

- `POST /api/hospital/claims`
- `GET /api/hospital/claims/{id}`
- `GET /api/hospital/claims`
- `PUT /api/hospital/claims/{id}/final-bill`

## Schema migration note

The claims schema was updated to use `claim_id` as the primary key (business identifier) instead of a numeric auto-increment `id`. If you have an existing database you must migrate data before deploying this change. Example migration steps (MySQL):

```sql
ALTER TABLE claims DROP PRIMARY KEY;
ALTER TABLE claims DROP COLUMN id;
ALTER TABLE claims ADD PRIMARY KEY (claim_id);
```

If you cannot drop `id` immediately, keep compatibility by populating `claim_id` for existing rows and adding a unique index, then switch the application after verifying data integrity.

## Quick run sequence (curl)

Make sure all three services are running on their default ports (hospital:9091, insurance:9092, claims:9093).

1) Create insurance (returns `insuranceId`)

```bash
curl -s -X POST http://localhost:9092/api/insurance/insurances \
	-H 'Content-Type: application/json' \
	-d '{"coverage":50000, "preauthPercentage":30, "endDate":"2030-12-31"}'
```

2) Create patient (returns `patientId`)

```bash
curl -s -X POST http://localhost:9091/api/hospital/patients \
	-H 'Content-Type: application/json' \
	-d '{"name":"Test Patient","insuranceId":"INS1001"}'
```

3) Create claim (returns `claimId`)

```bash
curl -s -X POST http://localhost:9093/api/claims \
	-H 'Content-Type: application/json' \
	-d '{"patientId":"PA1001","diagnosis":"Fever","estimatedAmount":25000}'
```

4) Update final bill by claimId (replace CLMxxxx with returned claimId)

```bash
curl -s -X PUT http://localhost:9093/api/claims/CLM1001/final-bill \
	-H 'Content-Type: application/json' \
	-d '{"finalBillAmount":22000}'
```

5) Process claim by underwriter (approve/reject)

```bash
curl -s -X POST http://localhost:9093/api/claims/CLM1001/process-claim \
	-H 'Content-Type: application/json' \
	-d '{"action":"approve","comment":"Underwriter ok"}'
```

Notes

- The Bruno collection in `bruno/MediSure` contains request examples and a suggested run order in its README. Use `environments/local.bru` to prefill values, or let the Create requests populate environment variables when run in sequence.

