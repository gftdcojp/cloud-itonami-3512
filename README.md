# cloud-itonami-3512

Open Business Blueprint for **ISIC Rev.5 3512**: transmission and distribution
of electric power, focused on community renewable-energy operations.

This repository designs a forkable OSS business for local operators who help
schools, cooperatives, municipalities and small firms run solar/storage assets,
meter energy flows, forecast demand, and publish auditable community-benefit
reports.

## Core Contract

```text
site telemetry + tariffs + community rules
        |
        v
Energy Advisor -> Grid/Policy Governor -> dispatch, hold, or human approval
        |
        v
audit ledger + settlement report
```

The advisor may recommend dispatch, maintenance or tariff actions, but cannot
change dispatch, billing or public claims unless the governor allows it.

## Runbook

- Start with read-only telemetry ingestion.
- Add governed recommendations for dispatch and battery operation.
- Add human approval for settlement-affecting actions.
- Publish monthly impact and audit reports.

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

Code and implementation templates are AGPL-3.0-or-later.
