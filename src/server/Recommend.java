package server;

import static utils.JSONUtils.toJSONObject;
import static utils.ServerUtils.authenticateUser;
import static utils.ServerUtils.writeJSONArray;

import com.google.common.collect.Sets;
import entity.EventObj;
import entity.User;
import external.TicketMasterApi;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@WebServlet(name = "recommend")
public class Recommend extends HttpServlet {

  private static final String LAT_KEY = "lat";
  private static final String LONG_KEY = "lon";
  private static final int TOP_GENRE = 5;
  private final MongoDBConnection mongoDBConnection = new MongoDBConnection();
  private final TicketMasterApi ticketMasterApi = new TicketMasterApi();

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setStatus(404);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    Optional<User> user = authenticateUser(request);
    if (!user.isPresent()) {
      System.out.println("No cookie found for the request.");
      response.setStatus(403);
      return;
    }
    if (request.getParameter(LAT_KEY) == null || request.getParameter(LONG_KEY) == null) {
      System.out.println("Lat or Long is missing from the request");
      response.setStatus(404);
      return;
    }

    System.out.println("Get event recommendation for user " + user.get().userId());
    String lat = request.getParameter(LAT_KEY);
    String lon = request.getParameter(LONG_KEY);

    Set<String> favouriteEventIds = mongoDBConnection.getFavouriteEventIds(user.get());
    System.out.println("User favourite events: " + favouriteEventIds);

    List<EventObj> events = mongoDBConnection.getEvents(favouriteEventIds);
    Map<String, Integer> genreCount = new HashMap<>();
    List<String> genres = events.stream().map(event -> event.genre())
        .flatMap(genre -> genre.stream()).collect(Collectors.toList());
    System.out.println("genres: " + genres);
    for (String genre : genres) {
      genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
    }

    List<Map.Entry<String, Integer>> sortedGenres = genreCount.entrySet().stream()
        .sorted((entry1, entry2) -> entry2.getValue() - entry1.getValue())
        .collect(Collectors.toList());
    System.out.println("sortedGenres " + sortedGenres);

    Set<String> topGenres = sortedGenres
        .subList(0, sortedGenres.size() > TOP_GENRE ? TOP_GENRE : sortedGenres.size()).stream()
        .map(entry -> entry.getKey()).collect(Collectors.toSet());

    List<EventObj> recommendation = ticketMasterApi.searchEventGeo(lat, lon).stream()
        .filter(event -> !Sets.intersection(event.genre(), topGenres).isEmpty())
        .collect(Collectors.toList());

    JSONArray array = new JSONArray();
    for (EventObj event : recommendation) {
      array.put(toJSONObject(event, favouriteEventIds.contains(event.eventId())));
    }

    writeJSONArray(response, array);
  }
}
