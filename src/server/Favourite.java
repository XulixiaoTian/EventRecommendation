package server;

import static utils.JSONUtils.toJSONObject;
import static utils.ServerUtils.authenticateUser;
import static utils.ServerUtils.readJsonObject;
import static utils.ServerUtils.writeJSONArray;

import entity.EventObj;
import entity.User;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongodb.MongoDBConnection;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "favourite")
public class Favourite extends HttpServlet {

  private static final String FAVOURITE_KEY = "favourite";
  private static final String REMOVE_FAVOURITE_KEY = "removeFavourite";
  private final MongoDBConnection mongoDBConnection = new MongoDBConnection();

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Optional<User> user = authenticateUser(request);
    if (!user.isPresent()) {
      System.out.println("No cookie found for the request.");
      response.setStatus(403);
      return;
    }

    Optional<Set<String>> favouriteIds = getFavouriteIds(request);
    if (!favouriteIds.isPresent()) {
      System.out.println("favourite key is missing from the request payload");
      response.setStatus(404);
      return;
    }

    mongoDBConnection.upsertFavouriteEvents(user.get(), favouriteIds.get());
    response.setStatus(200);
  }

  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Optional<User> user = authenticateUser(request);
    if (!user.isPresent()) {
      System.out.println("No cookie found for the request.");
      response.setStatus(403);
      return;
    }

    Optional<Set<String>> favouriteIds = getFavouriteIds(request);
    if (!favouriteIds.isPresent()) {
      System.out.println("favourite key is missing from the request payload");
      response.setStatus(404);
      return;
    }

    mongoDBConnection.removeFavouriteEvents(user.get(), favouriteIds.get());
    response.setStatus(200);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Optional<User> user = authenticateUser(request);
    if (!user.isPresent()) {
      System.out.println("No cookie found for the request.");
      response.setStatus(403);
      return;
    }
    Set<String> favouriteEventIds = mongoDBConnection.getFavouriteEventIds(user.get());
    List<EventObj> events = mongoDBConnection.getEvents(favouriteEventIds);
    JSONArray array = new JSONArray();
    for (EventObj event : events) {
      array.put(toJSONObject(event, true));
    }

    writeJSONArray(response, array);
  }

  private Optional<Set<String>> getFavouriteIds(HttpServletRequest request) {
    JSONObject payload = readJsonObject(request);
    if (payload == null) {
      return Optional.empty();
    }
    JSONArray favourites = payload.getJSONArray(FAVOURITE_KEY);
    return Optional
        .of(favourites.toList().stream().map(f -> (String) f).collect(Collectors.toSet()));
  }
}
