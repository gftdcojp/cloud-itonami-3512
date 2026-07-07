# ADR-0001: Energy Advisor ⊣ Grid Policy Governor architecture

## Status

Accepted. `cloud-itonami-isic-3512` promoted from `:blueprint` to
`:implemented` in the `kotoba-lang/industry` registry.

## Context

`cloud-itonami-isic-3512` publishes an OSS business blueprint for
community renewable-energy operations: helping schools, cooperatives,
municipalities and small firms run solar/storage assets, meter energy
flows, forecast demand, and publish auditable community-benefit
reports, run by a qualified operator so a community keeps its own
telemetry and settlement records instead of renting a closed SaaS.
Like every prior actor in this fleet, the blueprint alone is not an
implementation: this ADR records the governed-actor architecture that
promotes it to real, tested code, following the same langgraph-clj
StateGraph + independent Governor + Phase 0→3 rollout pattern
established by `cloud-itonami-isic-6511` (life insurance) and applied
across forty-eight prior siblings, most recently `cloud-itonami-isic-
2610` (semiconductor fab).

## Decision

### Decision 1: this fleet's THIRD infrastructure/utility vertical

Following `3600` (water-safety operations) and `6190` (telecom
access), `cloud-itonami-isic-3512` is this fleet's THIRD
infrastructure/utility vertical. The distinguishing concern here is
grid-interconnected distributed-energy-resource operation: battery
state-of-charge safety and grid-instability containment, genuinely
distinct from `3600`'s water-quality/threshold-breach concerns and
`6190`'s numbering/billing concerns.

### Decision 2: entity and op shape

The primary entity is a `site` (a community renewable-energy site --
solar/storage asset, analogous to `water.store`'s `site`). Five ops:
`:site/intake` (directory upsert, no capital risk), `:tariff/verify`
(per-jurisdiction grid-interconnection/tariff evidence checklist,
never auto), `:demand/screen` (grid-instability screening,
unconditional-evaluation discipline, never auto), `:actuation/
dispatch-battery` (POSITIVE, high-stakes -- dispatching the real
battery charge/discharge or switching action), and `:actuation/
finalize-settlement` (POSITIVE, high-stakes -- finalizing the real
tariff/settlement report). This matches the dual-actuation-on-one-
entity shape every recent dual-actuation sibling uses, grounded
directly in this blueprint's own published Core Contract ("The
advisor may recommend dispatch, maintenance or tariff actions, but
cannot change dispatch, billing or public claims unless the governor
allows it") and Trust Controls ("dispatch-affecting actions require
policy checks", "settlement-affecting actions require human
approval").

### Decision 3: `battery-soc-out-of-range?` -- the 5th two-sided range check

Following `testlab.registry/within-tolerance?` (1st), `conservation.
registry/body-condition-out-of-range?` (2nd), `water.registry/
contaminant-level-out-of-range?` (3rd) and `aerospace.registry/
assembly-tolerance-out-of-range?` (4th), `energy.registry/battery-
soc-out-of-range?` applies the SAME lo/hi-bounds-comparison shape to a
site's own measured battery state-of-charge against the site's own
recorded safe operating-range bounds -- a direct, natural mapping onto
real battery-management-system safety practice (both over-charge and
over-discharge are real failure modes, unlike a single-sided ceiling
or floor). Gates only `:actuation/dispatch-battery`.

### Decision 4: `grid-instability-flag-unresolved-violations` -- the 33rd unconditional-evaluation screening grounding, genuinely new concept

Before writing this check's docstring, every prior sibling's
`governor.cljc` was grepped for `grid-instability`/`curtailment` --
ZERO hits, confirming this is a genuinely new concept (not a reuse),
avoiding the false-precedent-claim risk `leasing`'s ADR-0001
documents and applying the verification discipline `union`'s and
`congregation`'s own ADR-0001s established. `grid-instability-flag-
unresolved-violations` reuses the unconditional-evaluation DISCIPLINE
(`casualty.governor/sanctions-violations`'s original fix) for the
33rd distinct application overall, continuing the count established
across this window's builds (water=25th, telecom=26th, aerospace=
27th, recovery=28th, consulting=29th, union=30th, congregation=31st,
fab=32nd, energy=33rd). Gates `:demand/screen` and `:actuation/
finalize-settlement` specifically -- matching this blueprint's own
Trust Control that public/settlement claims "must reference measured
data", i.e. cannot be finalized while a measured grid-instability
concern remains open.

### Decision 5: dedicated double-actuation-guard booleans

`:battery-dispatched?`/`:settlement-finalized?` are dedicated booleans
on the `site` record, never a single `:status` value -- the same
discipline every prior sibling governor's guards establish, informed
by `cloud-itonami-isic-6492`'s real status-lifecycle bug
(ADR-2607071320).

### Decision 6: Store protocol, MemStore + DatomicStore parity

`energy.store/Store` is implemented by both `MemStore` (atom-backed,
default for dev/tests/demo) and `DatomicStore` (`langchain.db`-
backed), proven to satisfy the same contract in `test/energy/
store_contract_test.clj` -- the same seam every sibling actor uses so
swapping the SSoT backend is a configuration change, not a rewrite.

### Decision 7: Phase 0→3 rollout

Phase 3's `:auto` set has exactly one member, `:site/intake` (no
capital risk). `:tariff/verify` and `:demand/screen` are never auto-
eligible at any phase (matching every sibling's screening-op
posture), and `:actuation/dispatch-battery`/`:actuation/finalize-
settlement` are permanently excluded from every phase's `:auto` set
-- a structural fact, not a rollout milestone, enforced by BOTH
`energy.phase` and `energy.governor`'s `high-stakes` set
independently.

### Decision 8: no bespoke domain capability lib

This vertical's site records are practice-specific rather than a
shared cross-operator data contract, so `energy.*` runs on the generic
robotics/telemetry/optimization/dmn/bpmn/audit-ledger/forms stack only
-- the same posture `9412`/`8720`/`8521`/`3030`/`3830`/`7020`/`9420`/
`9491` and others without a bespoke capability lib already establish.

### Decision 9: mock + LLM advisor pair

`energy.energyadvisor` provides `mock-advisor` (deterministic, default
everywhere -- the actor graph and governor contract run offline) and
`llm-advisor` (backed by `langchain.model/ChatModel`, with a defensive
EDN-proposal parser so a malformed LLM response degrades to a safe
low-confidence noop rather than ever auto-dispatching a battery action
or auto-finalizing a settlement).

### Decision 10: blueprint.edn field-sync fixes

Two stale-scaffold inconsistencies in `blueprint.edn`, discovered
during the standard "survey blueprint scaffold" step before writing
any code, were fixed as part of this promotion (the same class of fix
`card.6619`'s, `water.3600`'s, `telecom.6190`'s, `aerospace.3030`'s
and `fab.2610`'s own ADR-0001s document):

1. `:itonami.blueprint/id` was the stale pre-rename value
   `"cloud-itonami-3512"` (missing `isic-`), while the repo folder,
   README title and this actor's own `:business-id` already use the
   corrected `cloud-itonami-isic-3512`. Fixed to match.
2. `:itonami.blueprint/required-technologies`/`:optional-technologies`
   were missing entirely despite the `kotoba-lang/industry` registry's
   own entry for `"3512"` already stating `[:robotics :telemetry
   :optimization :dmn :bpmn :audit-ledger :forms]` / `[:cfd :cae]`.
   Fixed to match the registry exactly.

## Alternatives considered

- **A single "grid-policy-violation" check merging battery-SOC and
  grid-instability concerns.** Rejected: battery SOC is a ground-truth
  numeric recompute needing no proposal inspection; grid-instability
  status is an unconditionally-evaluated flag that must also HARD-hold
  the screening op itself on its own finding -- merging them would
  lose the screening op's self-hold property, the same reasoning
  `fab`'s ADR-0001 documents for its analogous yield-rate/process-
  defect distinction.
- **Modeling "public impact claims" as a third actuation** (alongside
  dispatch and settlement, per the blueprint's own Core Contract
  wording "cannot change dispatch, billing or public claims").
  Rejected for this R0: folding the impact-claim concern into the
  evidence-incomplete check for `:actuation/finalize-settlement` (a
  settlement/report cannot be finalized without evidence backing)
  captures the same Trust Control ("public impact claims must
  reference measured data") without inventing a third, less
  established actuation shape not shared by any sibling.

## Consequences

- Forty-ninth actor in this fleet (48 implemented before this build),
  and the THIRD infrastructure/utility vertical.
- Confirms the two-sided range check family generalizes to a fifth,
  genuinely distinct domain (battery-management safety), following
  `testlab`/`conservation`/`water`/`aerospace`.
- Establishes a genuinely NEW unconditional-evaluation-screening
  concept (grid-instability-flag), grep-verified absent from every
  prior sibling before the claim was finalized.
- `MemStore` ‖ `DatomicStore` parity is proven by `test/energy/
  store_contract_test.clj`, the same `:db-api`-driven swap pattern
  every sibling actor uses.
- Two pre-existing `blueprint.edn` inconsistencies (stale ID, missing
  required/optional-technologies fields) fixed as in-scope minor
  consistency work.
