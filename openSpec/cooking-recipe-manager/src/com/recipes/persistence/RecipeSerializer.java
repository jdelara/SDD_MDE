package com.recipes.persistence;

import com.recipes.model.Annotation;
import com.recipes.model.Recipe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RecipeSerializer {

    public static JSONObject toJson(Recipe recipe) {
        JSONObject obj = new JSONObject();
        obj.put("id", recipe.getId());
        obj.put("name", recipe.getName());
        obj.put("description", recipe.getDescription());
        obj.put("servings", recipe.getServings());

        JSONArray ings = new JSONArray();
        for (String s : recipe.getIngredients()) ings.put(s);
        obj.put("ingredients", ings);

        JSONArray steps = new JSONArray();
        for (String s : recipe.getInstructions()) steps.put(s);
        obj.put("instructions", steps);

        JSONArray tags = new JSONArray();
        for (String t : recipe.getTags()) tags.put(t);
        obj.put("tags", tags);

        JSONArray anns = new JSONArray();
        for (Annotation a : recipe.getAnnotations()) {
            JSONObject ao = new JSONObject();
            ao.put("id", a.getId());
            ao.put("text", a.getText());
            ao.put("createdAt", a.getCreatedAt());
            anns.put(ao);
        }
        obj.put("annotations", anns);

        return obj;
    }

    public static Recipe fromJson(JSONObject obj) {
        String id = obj.getString("id");
        String name = obj.getString("name");
        String description = obj.optString("description", "");
        int servings = obj.optInt("servings", 1);

        List<String> ingredients = new ArrayList<>();
        JSONArray ingArr = obj.optJSONArray("ingredients");
        if (ingArr != null) {
            for (int i = 0; i < ingArr.length(); i++) ingredients.add(ingArr.getString(i));
        }

        List<String> instructions = new ArrayList<>();
        JSONArray insArr = obj.optJSONArray("instructions");
        if (insArr != null) {
            for (int i = 0; i < insArr.length(); i++) instructions.add(insArr.getString(i));
        }

        List<String> tags = new ArrayList<>();
        JSONArray tagsArr = obj.optJSONArray("tags");
        if (tagsArr != null) {
            for (int i = 0; i < tagsArr.length(); i++) tags.add(tagsArr.getString(i));
        }

        List<Annotation> annotations = new ArrayList<>();
        JSONArray annArr = obj.optJSONArray("annotations");
        if (annArr != null) {
            for (int i = 0; i < annArr.length(); i++) {
                JSONObject ao = annArr.getJSONObject(i);
                annotations.add(Annotation.of(
                        ao.getString("id"),
                        ao.getString("text"),
                        ao.getString("createdAt")));
            }
        }

        return Recipe.of(id, name, description, ingredients, servings, instructions, tags, annotations);
    }
}
