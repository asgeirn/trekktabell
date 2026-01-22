# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Norwegian tax withholding table calculator (trekktabell) for 2026. Fork of Skatteetaten/trekktabell that provides a REST API for calculating tax withholding amounts. Calculates "forskuddstrekk" (advance tax deductions) based on tax tables, income amounts, and payment periods.

## Build Commands

```bash
mvn package          # Build JAR and run tests
mvn test             # Run tests only
mvn clean            # Clean build artifacts
```

Output artifacts:
- `target/trekkrutine-2026-0-SNAPSHOT.jar` - Main application
- `target/libs/` - Dependencies
- `target/bom.json` - SBOM

## Running the Application

```bash
java -jar target/trekkrutine-2026-0-SNAPSHOT.jar
```

REST API runs on port 8080. Example query:
```bash
curl 'http://localhost:8080/2026?table=7100&amount=50000'
```

## Architecture

### Core Calculation Flow

`Trekk.java` (REST API) → `Trekkrutine.java` (calculation engine) → returns withholding amount in øre

The calculation pipeline:
1. Round income to period-specific increment
2. Annualize the amount based on payment period
3. Calculate deductions via `Fradrag.java` (standard, minimum, seafarer, Finnmark)
4. Calculate taxes via `Skatteberegning.java` (municipal, state, progressive tiers, employer contributions)
5. Distribute back to payment period

### Key Classes

- **`Trekkrutine.java`** - Main calculation engine, entry point is `beregnTrekk()`
- **`Konstanter.java`** - 2026 tax rates, thresholds, and deduction limits (updated annually)
- **`Tabellnummer.java`** - Enum defining 100+ tax tables with their deduction percentages
- **`Periode.java`** - Enum for 7 payment periods (MAANED, UKE, DAG, etc.)
- **`Tabelltype.java`** - Enum for table types (VANLIG, STANDARDFRADRAG, SJO, FINNMARK, SPESIAL)
- **`Trekk.java`** - Undertow HTTP server exposing the REST API

### Package Structure

```
no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2026/
  - Tax calculation logic and constants
no.twingine/
  - REST API server
```

## Testing

JUnit 4 tests in `TrekkrutineTest.java`. Key test patterns:
- Zero income returns zero withholding
- Withholding never exceeds gross income
- More deductions result in lower withholding
- Full table generation tests (`kontrollerHeleTabellenAlle`)

Run single test class:
```bash
mvn test -Dtest=TrekkrutineTest
```

## Annual Updates

When updating for a new tax year:
1. Update `Konstanter.java` with new tax rates and thresholds
2. Rename package from `trekkrutine2026` to new year
3. Update `pom.xml` artifact name
4. Update REST API endpoint in `Trekk.java`
5. Update `Dockerfile` CMD to reference the new JAR name (e.g., `trekkrutine-2027-0-SNAPSHOT.jar`)
6. Add new Deployment, Service, and HTTPRoute rule to `trekktabell.yaml` for the new year

## Deployment

Docker multi-stage build using Chainguard JRE runtime. Published to GHCR. Kubernetes manifests in `k8s/` directory.
