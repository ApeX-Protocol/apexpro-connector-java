package exchange.apexpro.connector.impl.utils.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class CostBigDecimalAdapter implements JsonDeserializer<BigDecimal>, JsonSerializer<BigDecimal> {

    public BigDecimal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        BigDecimal cost;
        try {
            cost = new BigDecimal(json.getAsString());
        } catch (NumberFormatException e) {
            cost = new BigDecimal(0);
        }
        return cost;
    }

    @Override
    public JsonElement serialize(BigDecimal src, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(src);
    }
}