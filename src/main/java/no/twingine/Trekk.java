package no.twingine;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2022.Periode;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2022.Tabellnummer;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2022.Trekkrutine;

public class Trekk {

    public static void main(String[] args) throws UnknownHostException {
        Undertow server = Undertow.builder()
                .addHttpListener(8080, InetAddress.getLocalHost().getHostAddress())
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        BigDecimal amount = new BigDecimal(exchange.getQueryParameters().get("amount").element());
                        long trekk = Trekkrutine.beregnTabelltrekk(Tabellnummer.TABELL_7109, Periode.PERIODE_1_MAANED,
                                amount.longValue());
                        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send(Long.toString(trekk));
                    }
                }).build();
        server.start();
    }

}
