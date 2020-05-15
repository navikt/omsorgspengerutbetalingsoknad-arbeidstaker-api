# omsorgspengerutbetalingsoknad-arbeidstaker-api
![CI / CD](https://github.com/navikt/omsorgspengerutbetalingsoknad-arbeidstaker-api/workflows/CI%20/%20CD/badge.svg)
![NAIS Alerts](https://github.com/navikt/omsorgspengerutbetalingsoknad-arbeidstaker-api/workflows/Alerts/badge.svg)

# Innholdsoversikt
* [1. Kontekst](#1-kontekst)
* [2. Funksjonelle Krav](#2-funksjonelle-krav)
* [3. Begrensninger](#3-begrensninger)
* [4. Prinsipper](#4-prinsipper)
* [5. Programvarearkitektur](#5-programvarearkitektur)
* [6. Kode](#6-kode)
* [7. Data](#7-data)
* [8. Infrastrukturarkitektur](#8-infrastrukturarkitektur)
* [9. Distribusjon av tjenesten (deployment)](#9-distribusjon-av-tjenesten-deployment)
* [10. Utviklingsmiljø](#10-utviklingsmilj)
* [11. Drift og støtte](#11-drift-og-sttte)

# 1. Kontekst
API - tjeneste for omsorgspengerutbetaling for arbeidstakere

# 2. Funksjonelle Krav
Denne tjenesten understøtter søknadsprosessen, samt eksponerer endepunkt for innsending av søknad om utvidet rett til omsorgspengerutbetaling for arbeidstakere.



# 3. Begrensninger

# 4. Prinsipper

# 5. Programvarearkitektur

# 6. Kode

# 7. Data
## Full Søknad - Innkommende
[Link til serialiseringstet](https://github.com/navikt/omsorgspengerutbetalingsoknad-arbeidstaker-api/blob/master/src/test/kotlin/no/nav/omsorgspengerutbetaling/SerDesTest.kt#L83)

## Full Søknad - Utgående
TODO: Link som over

# 8. Infrastrukturarkitektur

# 9. Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort med bruk av Github Actions.
[Omsorgspengerutbetaling-Arbeidstaker-API CI / CD](https://github.com/navikt/omsorgspengerutbetalingsoknad-arbeidstaker-api/actions)

Push til dev-* brancher vil teste, bygge og deploye til dev/staging miljø.
Push/merge til master branche vil teste, bygge og deploye til produksjonsmiljø.

# 10. Utviklingsmiljø
## Bygge Prosjekt
For å bygge kode, kjør:

```shell script
./gradlew clean build
```

## Kjøre Prosjekt
For å kjøre kode, kjør:

```shell script
./gradlew bootRun
```

# 11. Drift og støtte
## Logging
[Kibana](https://tinyurl.com/ydkqetfo)

# Metrics
n/a

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).

# Redis
Vi bruker Redis for mellomlagring. En instanse av Redis må være kjørene før deploy av applikasjonen. 
Dette gjøres manuelt med kubectl både i preprod og prod. Se [nais/doc](https://github.com/nais/doc/blob/master/content/redis.md)

1. `kubectl config use-context preprod-sbs`
2. `kubectl apply -f redis-config.yml`
