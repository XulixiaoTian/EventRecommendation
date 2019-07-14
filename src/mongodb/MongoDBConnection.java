package mongodb;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import entity.EventObj;
import entity.FavouriteEvent;
import entity.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.bson.Document;

/** Class Containing logics to connect to MongoDb, insert/remove/upsert entities. */
public class MongoDBConnection {

  private static final String DATABASE = "Events";
  private static final String USER_COLLECTION = "User";
  private static final String EVENT_COLLECTION = "Event";
  private static final String USER_FAVOURITE_COLLECTION = "UserFavourite";
  private static final MongoClientURI uri = new MongoClientURI(
      "MongoClientURI");
  private final MongoClient mongoClient = new MongoClient(uri);
  private final MongoDatabase database = mongoClient.getDatabase(DATABASE);

  public void upsertUser(User user) {
    if (getUser(user.userId()) == null) {
      insertUser(user);
    }
  }

  public void insertUser(User user) {
    MongoCollection<Document> userCollection = database.getCollection(USER_COLLECTION);
    userCollection.insertOne(user.toDocument());
  }

  @Nullable
  public User getUser(String userId) {
    MongoCollection<Document> userCollection = database.getCollection(USER_COLLECTION);
    Document userInDb = userCollection.find(eq("userId", userId)).first();
    if (userInDb != null) {
      return User.fromDocument(userInDb);
    }
    return null;
  }

  public void upsertEvents(List<EventObj> events) {
    Set<String> eventsInDb = getEvents(
        events.stream().map(EventObj::eventId).collect(Collectors.toSet()))
        .stream().map(EventObj::eventId).collect(Collectors.toSet());
    insertEvents(events.stream().filter(event -> !eventsInDb.contains(event.eventId()))
        .collect(Collectors.toList()));
  }

  public void insertEvents(List<EventObj> events) {
    if (events.isEmpty()) {
      return;
    }
    MongoCollection<Document> eventCollection = database.getCollection(EVENT_COLLECTION);
    eventCollection
        .insertMany(events.stream().map(EventObj::toDocument).collect(Collectors.toList()));
  }

  public List<EventObj> getEvents(Set<String> eventIds) {
    MongoCollection<Document> eventCollection = database.getCollection(EVENT_COLLECTION);
    List<EventObj> events = new ArrayList<>();
    for (Document document : eventCollection.find(in("eventId", eventIds))) {
      EventObj event = EventObj.fromDocument(document);
      if (event != null) {
        events.add(event);
      }
    }
    return events;
  }

  public void removeEvents(Set<String> eventIds) {
    MongoCollection<Document> eventCollection = database.getCollection(EVENT_COLLECTION);
    eventCollection.deleteMany(in("eventId", eventIds));
  }

  public void upsertFavouriteEvents(User user, Set<String> eventIds) {
    Set<String> currentFavourites = getFavouriteEventIds(user);
    insertFavouriteEvents(user, eventIds.stream().filter(id -> !currentFavourites.contains(id))
        .collect(Collectors.toSet()));
  }

  public void insertFavouriteEvents(User user, Set<String> eventIds) {
    if (eventIds.isEmpty()) {
      return;
    }
    MongoCollection<Document> eventCollection = database.getCollection(USER_FAVOURITE_COLLECTION);
    eventCollection.insertMany(eventIds.stream()
        .map(id -> FavouriteEvent.create(user.userId(), id)).map(FavouriteEvent::toDocument)
        .collect(Collectors.toList()));
  }

  public void removeFavouriteEvents(User user, Set<String> eventIds) {
    MongoCollection<Document> eventCollection = database.getCollection(USER_FAVOURITE_COLLECTION);
    eventCollection.deleteMany(and(in("eventId", eventIds), eq("userId", user.userId())));
  }

  public Set<String> getFavouriteEventIds(User user) {
    MongoCollection<Document> eventCollection = database.getCollection(USER_FAVOURITE_COLLECTION);
    Set<String> eventIds = new HashSet<>();
    for (Document document : eventCollection.find(eq("userId", user.userId()))) {
      FavouriteEvent event = FavouriteEvent.fromDocument(document);
      if (event != null) {
        eventIds.add(event.eventId());
      }
    }
    return eventIds;
  }

//  public static void main(String[] args) {
//    MongoDBConnection mongoDBConnection = new MongoDBConnection();
//    mongoDBConnection.insertEvents(mongoDBConnection.getEvents(ImmutableSet.of("vvG1HZ4E3VkaO4")));
//
//  }
}
