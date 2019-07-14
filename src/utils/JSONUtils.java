package utils;

import entity.EventObj;
import org.json.JSONArray;
import org.json.JSONObject;

/** Utils class for JASON serialization and de-serialization. */
public class JSONUtils {

  public static JSONObject toJSONObject(EventObj event, boolean favourite) {
    JSONObject obj = new JSONObject();
    try {

      StringBuilder addr = new StringBuilder(event.address1() + " " + event.address2());

      obj.put("name", event.name());
      obj.put("event_id", event.eventId());
      obj.put("address", addr.toString());
      obj.put("date", event.date());
      obj.put("categories", new JSONArray(event.categories()));
      obj.put("genres", new JSONArray(event.genre()));
      obj.put("image_url", event.imageURL().get(0));
      obj.put("url", event.url());
      obj.put("date", event.date());
      obj.put("favourite", favourite);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return obj;
  }
}
