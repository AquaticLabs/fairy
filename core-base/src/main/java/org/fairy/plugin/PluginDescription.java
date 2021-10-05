package org.fairy.plugin;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import org.fairy.library.Library;

import java.util.ArrayList;
import java.util.List;

@Data
public class PluginDescription {

    private final String name;
    private final String mainClass;
    private final String shadedPackage;
    private final List<String> modules;
    private final List<String> extensions;
    private final List<Library> libraries;

    public PluginDescription(JsonObject jsonObject) {
        Preconditions.checkArgument(jsonObject.has("name"), "name property could not be found.");
        Preconditions.checkArgument(jsonObject.has("mainClass"), "mainClass property could not be found.");
        Preconditions.checkArgument(jsonObject.has("shadedPackage"), "shadedPackage property could not be found.");

        this.name = jsonObject.get("name").getAsString();
        this.mainClass = jsonObject.get("mainClass").getAsString();
        this.shadedPackage = jsonObject.get("shadedPackage").getAsString();

        this.modules = new ArrayList<>();
        if (jsonObject.has("modules")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("modules")) {
                this.modules.add(jsonElement.getAsString());
            }
        }

        this.extensions = new ArrayList<>();
        if (jsonObject.has("extensions")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("extensions")) {
                this.extensions.add(jsonElement.getAsString());
            }
        }

        this.libraries = new ArrayList<>();
        if (jsonObject.has("libraries")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("libraries")) {
                this.libraries.add(Library.fromJsonObject(jsonElement.getAsJsonObject(), this.shadedPackage));
            }
        }
    }

}
