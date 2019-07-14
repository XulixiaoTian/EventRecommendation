package entity;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import org.bson.Document;

import javax.annotation.Nullable;
import java.io.IOException;

/** Favourite Event containing a userId and eventId. */
@AutoValue
public abstract class FavouriteEvent {

  @SerializedName("userId")
  public abstract String userId();

  @SerializedName("eventId")
  public abstract String eventId();

  public static FavouriteEvent create(String userId, String eventId) {
    return new AutoValue_FavouriteEvent(userId, eventId);
  }

  public Document toDocument() {
    Gson gson = new Gson();
    TypeAdapter<FavouriteEvent> userAdapter = FavouriteEvent.typeAdapter(gson);
    return Document.parse(userAdapter.toJson(this));
  }

  @Nullable
  public static FavouriteEvent fromDocument(Document document) {
    Gson gson = new Gson();
    TypeAdapter<FavouriteEvent> userAdapter = FavouriteEvent.typeAdapter(gson);
    try {
      return userAdapter.fromJson(document.toJson());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static TypeAdapter<FavouriteEvent> typeAdapter(Gson gson) {
    return new AutoValue_FavouriteEvent.GsonTypeAdapter(gson);
  }
}
