package no.twingine;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2023.Periode;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2023.Tabellnummer;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2023.Trekkrutine;

public class Trekk {

    public static void main(String[] args) throws UnknownHostException {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, InetAddress.getLocalHost().getHostAddress())
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        String table = "TABELL_" + exchange.getQueryParameters().get("table").element();
                        Tabellnummer tabellnummer = Arrays.stream(Tabellnummer.values())
                                .filter(it -> it.name().equals(table)).findFirst().get();
                        BigDecimal amount = new BigDecimal(exchange.getQueryParameters().get("amount").element());
                        long trekk = Trekkrutine.beregnTabelltrekk(tabellnummer, Periode.PERIODE_1_MAANED,
                                amount.longValue());
                        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send(Long.toString(trekk));
                    }
                }).build();
        server.start();
    }

}
