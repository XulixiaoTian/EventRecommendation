package entity;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.ticketmaster.discovery.model.Category;
import com.ticketmaster.discovery.model.Date;
import com.ticketmaster.discovery.model.Event;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.bson.Document;

/** Event object storing event information. */
@AutoValue
public abstract class EventObj {

  @SerializedName("eventId")
  public abstract String eventId();

  @SerializedName("name")
  public abstract String name();

  @SerializedName("address1")
  public abstract String address1();

  @SerializedName("address2")
  public abstract String address2();

  @SerializedName("categories")
  public abstract List<Category> categories();

  @SerializedName("imageURL")
  public abstract List<String> imageURL();

  @SerializedName("url")
  public abstract String url();

  @SerializedName("date")
  public abstract String date();

  @SerializedName("genre")
  public abstract Set<String> genre();

  @SerializedName("otherProperties")
  public abstract Map<String, Object> otherProperties();

  public static Builder builder() {
    return new AutoValue_EventObj.Builder();
  }

  private static String getEventDate(Date dates) {
    return Optional.ofNullable(dates).map(Date::getStart).map(Date.Start::getDateTime)
        .map(dt -> dt.toString("E MM/dd/yyyy HH:mm")).orElse("No date");
  }

  public static EventObj fromEvent(Event event) {
    return EventObj.builder().setEventId(event.getId())
        .setName(event.getName()).setUrl(event.getUrl())
        .setImageURL(
            event.getImages() != null ? event.getImages().stream().map(image -> image.getUrl())
                .collect(Collectors.toList()) : new ArrayList<>())
        .setCategories(event.getCategories() != null ? event.getCategories() : new ArrayList<>())
        .setAddress1(event.getVenues() != null ? Optional
            .ofNullable(event.getVenues().get(0).getAddress().getLine1()).orElse("") : "")
        .setAddress2(event.getVenues() != null ? Optional
            .ofNullable(event.getVenues().get(0).getAddress().getLine2()).orElse("") : "")
        .setDate(getEventDate(event.getDates()))
        .setGenre(Optional.ofNullable(event.getClassifications()).orElse(new ArrayList<>()).stream()
            .filter(c -> c.getGenre() != null && c.getGenre().getName() != null && !c.getGenre()
                .getName().equals("other")).map(c -> c.getGenre()).map(genre -> genre.getName())
            .collect(Collectors.toSet()))
        .setOtherProperties(event.getOtherProperties())
        .build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setEventId(String value);

    public abstract Builder setName(String value);

    public abstract Builder setAddress1(String value);

    public abstract Builder setAddress2(String value);

    public abstract Builder setCategories(List<Category> value);

    public abstract Builder setImageURL(List<String> value);

    public abstract Builder setUrl(String value);

    public abstract Builder setDate(String value);

    public abstract Builder setGenre(Set<String> value);

    public abstract Builder setOtherProperties(Map<String, Object> value);

    public abstract EventObj build();
  }

  public Document toDocument() {
    Gson gson = new Gson();
    TypeAdapter<EventObj> userAdapter = EventObj.typeAdapter(gson);
    return Document.parse(userAdapter.toJson(this));
  }

  @Nullable
  public static EventObj fromDocument(Document document) {
    Gson gson = new Gson();
    TypeAdapter<EventObj> userAdapter = EventObj.typeAdapter(gson);
    try {
      return userAdapter.fromJson(document.toJson());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static TypeAdapter<EventObj> typeAdapter(Gson gson) {
    return new AutoValue_EventObj.GsonTypeAdapter(gson);
  }
}
