# MediSure Bruno Collection

## Environment
Use `environments/local.bru`.

Default URLs:
- Hospital: `http://localhost:9091`
- Insurance: `http://localhost:9092`
- Claims: `http://localhost:9093`

## Suggested run order

1. `Create Insurance`
2. `Get Insurance By Id`
3. `Create Patient`
4. `Create Claim` (Claims)
5. `Get Claim By Id` (Claims)
6. `Get All Claims` (Claims)
7. `Update Final Bill Amount` (Claims)
8. `Get Claim By Id` (Claims)

## Notes
 - Claims `Create Claim` stores business `claimId` into `claimId`.
- `Create Patient` stores generated `patientId` into `patientId`.
- `Create Insurance` stores generated values like `INS101` into `insuranceId`.
- Claims `Create Claim` sends `patientId`, `diagnosis`, and `estimatedAmount`. The final bill is sent later through `Update Final Bill Amount`.
- Make sure all three services are running before sending requests.
