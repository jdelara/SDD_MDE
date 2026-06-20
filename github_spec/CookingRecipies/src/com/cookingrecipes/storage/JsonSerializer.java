package com.cookingrecipes.storage;

import com.cookingrecipes.model.*;
import java.util.*;

public class JsonSerializer {

    // ── Serialize ──────────────────────────────────────────────────────────

    public String serialize(List<Recipe> recipes, List<Tag> tags) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"tags\": ").append(serializeTags(tags)).append(",\n");
        sb.append("  \"recipes\": ").append(serializeRecipes(recipes)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String serializeTags(List<Tag> tags) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("{\"name\": ").append(jsonString(tags.get(i).getName())).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeRecipes(List<Recipe> recipes) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < recipes.size(); i++) {
            if (i > 0) sb.append(",\n");
            sb.append(serializeRecipe(recipes.get(i)));
        }
        sb.append("\n  ]");
        return sb.toString();
    }

    private String serializeRecipe(Recipe r) {
        StringBuilder sb = new StringBuilder("    {\n");
        sb.append("      \"id\": ").append(jsonString(r.getId())).append(",\n");
        sb.append("      \"name\": ").append(jsonString(r.getName())).append(",\n");
        sb.append("      \"description\": ").append(jsonString(r.getDescription())).append(",\n");
        sb.append("      \"serves\": ").append(r.getServes()).append(",\n");
        sb.append("      \"instructions\": ").append(jsonString(r.getInstructions())).append(",\n");
        sb.append("      \"tags\": ").append(serializeTagNames(r.getTags())).append(",\n");
        sb.append("      \"ingredients\": ").append(serializeIngredients(r.getIngredients())).append(",\n");
        sb.append("      \"annotations\": ").append(serializeAnnotations(r.getAnnotations())).append("\n");
        sb.append("    }");
        return sb.toString();
    }

    private String serializeTagNames(List<Tag> tags) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(jsonString(tags.get(i).getName()));
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeIngredients(List<Ingredient> ingredients) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) sb.append(", ");
            Ingredient ing = ingredients.get(i);
            sb.append("{\"name\": ").append(jsonString(ing.getName()))
              .append(", \"quantity\": ").append(jsonString(ing.getQuantity()))
              .append(", \"unit\": ").append(jsonString(ing.getUnit()))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeAnnotations(List<Annotation> annotations) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < annotations.size(); i++) {
            if (i > 0) sb.append(", ");
            Annotation a = annotations.get(i);
            sb.append("{\"text\": ").append(jsonString(a.getText()))
              .append(", \"timestamp\": ").append(jsonString(a.getTimestamp()))
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String jsonString(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:   sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    // ── Deserialize ────────────────────────────────────────────────────────

    public StoreData deserialize(String json) {
        List<Tag> tags = new ArrayList<>();
        List<Recipe> recipes = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return new StoreData(recipes, tags);

        try {
            JsonParser p = new JsonParser(json.trim());
            Map<String, Object> root = p.parseObject();

            Object tagsObj = root.get("tags");
            if (tagsObj instanceof List) {
                for (Object t : (List<?>) tagsObj) {
                    if (t instanceof Map) {
                        Object name = ((Map<?,?>) t).get("name");
                        if (name instanceof String) tags.add(new Tag((String) name));
                    }
                }
            }

            Object recipesObj = root.get("recipes");
            if (recipesObj instanceof List) {
                for (Object ro : (List<?>) recipesObj) {
                    if (ro instanceof Map) recipes.add(parseRecipe((Map<?,?>) ro));
                }
            }
        } catch (Exception e) {
            // Return empty store on parse error
        }
        return new StoreData(recipes, tags);
    }

    private Recipe parseRecipe(Map<?,?> m) {
        Recipe r = new Recipe();
        r.setId(str(m.get("id")));
        r.setName(str(m.get("name")));
        r.setDescription(str(m.get("description")));
        r.setInstructions(str(m.get("instructions")));
        Object sv = m.get("serves");
        if (sv instanceof Number) r.setServes(((Number) sv).intValue());

        Object tagsObj = m.get("tags");
        if (tagsObj instanceof List) {
            List<Tag> tags = new ArrayList<>();
            for (Object t : (List<?>) tagsObj) if (t instanceof String) tags.add(new Tag((String) t));
            r.setTags(tags);
        }

        Object ingsObj = m.get("ingredients");
        if (ingsObj instanceof List) {
            List<Ingredient> ings = new ArrayList<>();
            for (Object io : (List<?>) ingsObj) {
                if (io instanceof Map) {
                    Map<?,?> im = (Map<?,?>) io;
                    ings.add(new Ingredient(str(im.get("name")), str(im.get("quantity")), str(im.get("unit"))));
                }
            }
            r.setIngredients(ings);
        }

        Object annsObj = m.get("annotations");
        if (annsObj instanceof List) {
            List<Annotation> anns = new ArrayList<>();
            for (Object ao : (List<?>) annsObj) {
                if (ao instanceof Map) {
                    Map<?,?> am = (Map<?,?>) ao;
                    anns.add(new Annotation(str(am.get("text")), str(am.get("timestamp"))));
                }
            }
            r.setAnnotations(anns);
        }
        return r;
    }

    private String str(Object o) { return o instanceof String ? (String) o : ""; }

    // ── Minimal JSON Parser ────────────────────────────────────────────────

    private static class JsonParser {
        private final String src;
        private int pos;

        JsonParser(String src) { this.src = src; }

        Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWs();
            if (peek() == '}') { pos++; return map; }
            while (true) {
                skipWs();
                String key = parseString();
                skipWs(); expect(':'); skipWs();
                Object val = parseValue();
                map.put(key, val);
                skipWs();
                char c = next();
                if (c == '}') break;
                if (c != ',') throw new RuntimeException("Expected , or }");
            }
            return map;
        }

        List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWs();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                skipWs();
                list.add(parseValue());
                skipWs();
                char c = next();
                if (c == ']') break;
                if (c != ',') throw new RuntimeException("Expected , or ]");
            }
            return list;
        }

        Object parseValue() {
            skipWs();
            char c = peek();
            if (c == '"') return parseString();
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == 't') { pos += 4; return Boolean.TRUE; }
            if (c == 'f') { pos += 5; return Boolean.FALSE; }
            if (c == 'n') { pos += 4; return null; }
            return parseNumber();
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    char e = src.charAt(pos++);
                    switch (e) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        default: sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        Number parseNumber() {
            int start = pos;
            while (pos < src.length() && "-0123456789.eE+".indexOf(src.charAt(pos)) >= 0) pos++;
            String num = src.substring(start, pos);
            if (num.contains(".") || num.contains("e") || num.contains("E"))
                return Double.parseDouble(num);
            return Long.parseLong(num);
        }

        void skipWs() { while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++; }
        char peek() { return pos < src.length() ? src.charAt(pos) : 0; }
        char next() { return src.charAt(pos++); }
        void expect(char c) { if (next() != c) throw new RuntimeException("Expected " + c); }
    }

    public static class StoreData {
        public final List<Recipe> recipes;
        public final List<Tag> tags;
        StoreData(List<Recipe> recipes, List<Tag> tags) {
            this.recipes = recipes;
            this.tags = tags;
        }
    }
}
