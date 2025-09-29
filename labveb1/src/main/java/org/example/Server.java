package org.example;

import com.fastcgi.FCGIInterface;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Server {
    private static final String RESPONSE = """
            Content-Type: application/json; charset=UTF-8
            
            
            {"result":"%s","x":"%s","y":"%s","r":"%s","time":"%s","workTime":"%s","error":"all ok"}
            """;

    public static void main(String[] args) {
        var fcgi = new FCGIInterface();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        while (fcgi.FCGIaccept() >= 0) {
            try {
                String queryParams = System.getProperties().getProperty("QUERY_STRING");
                Parameters params = new Parameters(queryParams);

                Instant startTime = Instant.now();
                boolean result = calculate(params.getX(), params.getY(), params.getR());
                Instant endTime = Instant.now();
                if (result) {
                    String response = RESPONSE.formatted("OK", params.getX(), params.getY(), params.getR(),
                            LocalDateTime.now().format(formatter), ChronoUnit.NANOS.between(startTime, endTime));
                    System.out.println(response);
                } else {
                    String response = RESPONSE.formatted("NOT", params.getX(), params.getY(), params.getR(),
                            LocalDateTime.now().format(formatter), ChronoUnit.NANOS.between(startTime, endTime));
                    System.out.println(response);
                }
            } catch (ValidationException e) {
                String response = RESPONSE.formatted("BAD REQUEST", 0, 0, 0,
                        LocalDateTime.now().format(formatter), 0);
                System.out.println(response);
            }
        }
    }

    private static boolean calculate(float x, float y, float r) {
        if (x > 0 && y > 0) {
            return false;
        }
        if (x > 0 && y < 0) {
            if ((x * x + y * y) > (r * r)) {
                return false;
            }
        }
        if (x < 0 && y < 0) {
            if ((x + y) < -r / 2) {
                return false;
            }
        }
        if (x < 0 && y > 0) {
            if (x < -r / 2 || y > r) {
                return false;
            }
        }
        return true;
    }
}