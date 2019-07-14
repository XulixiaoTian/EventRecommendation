package server;

import static utils.JSONUtils.toJSONObject;
import static utils.ServerUtils.authenticateUser;
import static utils.ServerUtils.writeJSONArray;

import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import entity.EventObj;
import entity.User;
import external.TicketMasterApi;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mongodb.MongoDBConnection;
import org.json.JSONArray;

@WebServlet(name = "search")
public class Search extends HttpServlet {

  private static final String CITY_KEY = "city";
  private static final String KEYWORD_KEY = "keyword";
  private static final String LAT_KEY = "lat";
  private static final String LONG_KEY = "lon";
  private final TicketMasterApi ticketMasterApi = new TicketMasterApi();
  private final MongoDBConnection mongoDBConnection = new MongoDBConnection();

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
    SearchEventsOperation operation = new SearchEventsOperation();

    if (request.getParameter(CITY_KEY) != null) {
      operation.city(request.getParameter(CITY_KEY));
    }
    if (request.getParameter(KEYWORD_KEY) != null) {
      operation.keyword(request.getParameter(KEYWORD_KEY));
    }
    if (request.getParameter(LAT_KEY) != null && request.getParameter(LONG_KEY) != null) {
      operation.latlong(request.getParameter(LAT_KEY), request.getParameter(LONG_KEY));
    }

    if (operation.getQueryParameters().isEmpty()) {
      System.out.println("Missing searching criteria!");
      response.setStatus(404);
      return;
    }

    List<EventObj> events = ticketMasterApi.searchEvent(operation);
    mongoDBConnection.upsertEvents(events);
    Set<String> favouriteEventIds = mongoDBConnection.getFavouriteEventIds(user.get());
    JSONArray array = new JSONArray();
    for (EventObj event : events) {
      array.put(toJSONObject(event, favouriteEventIds.contains(event.eventId())));
    }

    writeJSONArray(response, array);
  }
}
