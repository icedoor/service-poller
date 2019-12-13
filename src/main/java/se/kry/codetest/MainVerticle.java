package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, Service> services = new HashMap<>();
  private DBConnector connector;
  private BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    FetchFromDatabase();

    WebClient client = WebClient.create(vertx);
    vertx.setPeriodic(1000 * 5, timerId -> poller.pollServices(services, client));
    setRoutes(router);
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8080, result -> {
        if (result.succeeded()) {
          System.out.println("KRY code test service started");
          startFuture.complete();
        } else {
          startFuture.fail(result.cause());
        }
      });
  }

  private void FetchFromDatabase() {
    String sql = "SELECT url, name, added FROM service";
    connector.query(sql).setHandler(done -> {
      if (done.succeeded()) {
        done.result().getResults().forEach(json -> {
          String urlString = json.getString(0);
          String name = json.getString(1);
          long timeAdded = json.getLong(2);
          URL url;
          try {
            url = new URL(urlString);
          } catch (MalformedURLException e) {
            return;
          }
          Service service = new Service(url, name, timeAdded);
          services.put(urlString, service);
        });

        if (services.isEmpty()) {
          // Add one default service
          try {
            AddService("https://www.kry.se", "Kry");
          } catch (Exception e) {
            System.out.println("Could not add service");
          }
        }
      } else {
        done.cause().printStackTrace();
      }
    });
  }

  private void DeleteService(String urlString) {
    services.remove(urlString);

    String sql = "DELETE FROM service WHERE url=?";
    JsonArray insertparams = new JsonArray().add(urlString);
    connector.query(sql,  insertparams).setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("Service removed from db");
      } else {
        done.cause().printStackTrace();
      }
    });
  }

  private void AddService(String urlString, String name) throws Exception {
    URL url = new URL(urlString);
    String insertName = name != null ? name : url.getHost();
    long timeNow = System.currentTimeMillis();
    Service service = new Service(url, insertName, timeNow);
    services.put(urlString, service);

    String sql = "INSERT INTO service (url , name, added)  VALUES (?, ?, ?)";
    JsonArray insertparams = new JsonArray().add(urlString).add(insertName).add(timeNow);
    connector.query(sql,  insertparams).setHandler(done -> {
      if (done.succeeded()) {
        System.out.println("Service inserted into db");
      } else {
        done.cause().printStackTrace();
      }
    });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    router.delete("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();

      try {
        DeleteService(jsonBody.getString("url"));
      } catch (Exception e) {
        req.response()
                .putHeader("content-type", "text/plain")
                .end("Url parse error");
        return;
      }
      req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
    });
    router.get("/service").handler(req -> {
      List<JsonObject> jsonServices = services
              .entrySet()
              .stream()
              .map(service ->
                      new JsonObject()
                              .put("url", service.getKey())
                              .put("name", service.getValue().getName())
                              .put("timeadded", service.getValue().getTimestamp())
                              .put("status", service.getValue().getStatus()))
              .collect(Collectors.toList());
      req.response()
              .putHeader("content-type", "application/json")
              .end(new JsonArray(jsonServices).encode());
    });
    router.post("/service").handler(req -> {
      JsonObject jsonBody = req.getBodyAsJson();

      try {
        AddService(jsonBody.getString("url"), jsonBody.getString("name"));
      } catch (Exception e) {
        req.response()
                .putHeader("content-type", "text/plain")
                .end("Url parse error");
        return;
      }

      req.response()
              .putHeader("content-type", "text/plain")
              .end("OK");
    });
  }
}
