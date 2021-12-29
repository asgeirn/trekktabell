package no.twingine;

import java.io.PrintWriter;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2022.Periode;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2022.Tabellnummer;
import no.skatteetaten.fastsetting.formueinntekt.forskudd.trekkrutine2022.Trekkrutine;

public class Trekk implements HttpFunction {

    @Override
    public void service(HttpRequest req, HttpResponse res) throws Exception {
        long amount = Long.parseLong(req.getFirstQueryParameter("amount").orElse("0"));
        long trekk = Trekkrutine.beregnTabelltrekk(Tabellnummer.TABELL_7109, Periode.PERIODE_1_MAANED, amount);
        res.setContentType("text/plain");
        try (PrintWriter pw = new PrintWriter(res.getWriter())) {
            pw.println(trekk);
        }
    }

}
