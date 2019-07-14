package entity;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import javax.annotation.Nullable;
import org.bson.Document;

/** Contains User related information. */
@AutoValue
public abstract class User {

  @SerializedName("userId")
  public abstract String userId();

  @SerializedName("email")
  public abstract String email();

  @SerializedName("name")
  public abstract String name();

  @SerializedName("firstName")
  public abstract String firstName();

  @SerializedName("lastName")
  public abstract String lastName();

  @SerializedName("pictureUrl")
  public abstract String pictureUrl();

  public static Builder builder() {
    return new AutoValue_User.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setUserId(String value);

    public abstract Builder setEmail(String value);

    public abstract Builder setName(String value);

    public abstract Builder setFirstName(String value);

    public abstract Builder setLastName(String value);

    public abstract Builder setPictureUrl(String value);

    public abstract User build();
  }

  public Document toDocument() {
    Gson gson = new Gson();
    TypeAdapter<User> userAdapter = User.typeAdapter(gson);
    return Document.parse(userAdapter.toJson(this));
  }

  @Nullable
  public static User fromDocument(Document document) {
    Gson gson = new Gson();
    TypeAdapter<User> userAdapter = User.typeAdapter(gson);
    try {
      return userAdapter.fromJson(document.toJson());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static TypeAdapter<User> typeAdapter(Gson gson) {
    return new AutoValue_User.GsonTypeAdapter(gson);
  }
}
