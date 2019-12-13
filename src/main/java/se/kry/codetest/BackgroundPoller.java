package se.kry.codetest;

import io.vertx.core.Future;

import java.net.URL;
import java.util.List;
import java.util.Map;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;

public class BackgroundPoller {

  public Future<List<String>> pollServices(Map<String, Service> services, WebClient client) {

    services.forEach((key, service) -> {
        URL url = service.getUrl();
        client.get(443, url.getHost(), url.getPath()).ssl(url.getProtocol().equals("https"))
          .send(res -> {
            if (res.succeeded()) {
                service.setStatus("OK");
            } else {
                service.setStatus("FAIL");
            }
          });
    });

    return Future.failedFuture("TODO");
  }
}
