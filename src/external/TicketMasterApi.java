package external;

import com.ticketmaster.api.discovery.DiscoveryApi;
import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.discovery.model.Events;
import entity.EventObj;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import mongodb.MongoDBConnection;

/** Class containing Ticket Master Api related logic. */
public class TicketMasterApi {

  private static final String TICKET_MASTER_API_KEY = System
      .getProperty("ticketmaster-api-key", "TICKET_MASTER_API_KEY");
  private static final int PAGE_SIZE = 50;
  private static final int DEFAULT_RADIUS = 50;
  private final DiscoveryApi api = new DiscoveryApi(TICKET_MASTER_API_KEY);

  public List<EventObj> searchEvent(SearchEventsOperation operation) {
    try {
      PagedResponse<Events> page = api.searchEvents(operation.pageSize(PAGE_SIZE));
      if (page.getPageInfo().getTotalElements() == 0) {
        return new ArrayList<>();
      }
      return page.getContent().getEvents().stream().map(EventObj::fromEvent)
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println(
          String.format("Failed to query TicketMaster with SearchEventsOperation %s", operation));
    }
    return new ArrayList<>();
  }


  public List<EventObj> searchEventKeyword(String searchTerm) {
    return searchEvent(new SearchEventsOperation().keyword(searchTerm));
  }

  public List<EventObj> searchEventCity(String city) {
    return searchEvent(new SearchEventsOperation().city(city));
  }

  public List<EventObj> searchEventGeo(String latitude, String longitude) {
    return searchEvent(
        new SearchEventsOperation().radius(DEFAULT_RADIUS).latlong(latitude, longitude));
  }

  public static void main(String[] args) {
    TicketMasterApi ticketMasterApi = new TicketMasterApi();
    MongoDBConnection mongoDBConnection = new MongoDBConnection();
    List<EventObj> seattle = ticketMasterApi.searchEventCity("seattle");
  }
}
