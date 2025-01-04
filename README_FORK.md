# Fork README

Denne kopien av `Skatteetaten/trekktabell` inneholder en Undertow servlet for å tilby trekktabellen som et REST-API.

HTTP parametrene `amount` og `table` brukes for å angi beløp og tabellnummer.  Den svarer på alle forespørsler uavhengig
av path.

Trekktabeller for 2023, 2024 og 2025 er tilgjengelige på https://trekktabell.twingine.com/<år>

Så hvis du har tabell 8200 i 2025 og tjener 65535 kroner brutto per måned kan du få beregnet skattetrekk med følgende:

```
curl 'https://trekktabell.twingine.com/2025?table=8200&amount=65535'
```

## Personvern

Forespørsler blir ikke logget, men går gjennom Cloudflare.  Ettersom skattetrekk beregnes etter hele hundre kroner kan du
(delvis) anonymisere forespørslene dine ved å runde ned til nærmeste hundre.
