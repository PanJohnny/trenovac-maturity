package me.panjohnny.trenovacmaturity.model;

import com.google.gson.JsonElement;

public interface JsonSerializable {
    JsonElement serialize();

    static <T extends JsonSerializable> T deserialize(JsonElement element) {
        throw new UnsupportedOperationException("Deserialize method not implemented");
    }
}
