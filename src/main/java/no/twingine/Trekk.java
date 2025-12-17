package no.twingine;

import io.undertow.Undertow;
import io.undertow.util.Headers;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2026.Periode;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2026.Tabellnummer;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2026.Trekkrutine;

public class Trekk {

    public static void main(String[] args) throws UnknownHostException {
        Undertow server = Undertow.builder()
            .addHttpListener(8080, InetAddress.getLocalHost().getHostAddress())
            .setHandler(exchange -> {
                String table =
                    "TABELL_" +
                    exchange.getQueryParameters().get("table").element();
                Tabellnummer tabellnummer = Arrays.stream(Tabellnummer.values())
                    .filter(it -> it.name().equals(table))
                    .findFirst()
                    .orElseThrow();
                BigDecimal amount = new BigDecimal(
                    exchange.getQueryParameters().get("amount").element()
                );
                long trekk = Trekkrutine.beregnTabelltrekk(
                    tabellnummer,
                    Periode.PERIODE_1_MAANED,
                    amount.longValue()
                );
                exchange
                    .getResponseHeaders()
                    .add(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send(Long.toString(trekk));
            })
            .build();
        server.start();
    }
}
