package com.minenash.customhud_limelight_integration;

import io.wispforest.limelight.api.entry.*;
import io.wispforest.limelight.api.extension.LimelightExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TagsExtension implements LimelightExtension {
    public static final Identifier ID = Identifier.of("limelight_tags", "entry");
    public static final TagsExtension INSTANCE = new TagsExtension();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public @Nullable ResultEntryGatherer checkExclusiveGatherer(ResultGatherContext ctx) {
        String text = ctx.searchText();
        if (!text.startsWith("#")) return null;
        text = text.substring(1);

        Identifier id = Identifier.tryParse(text);
        if (id == null) return null;

        String fText = text;
        return (ctx1, entryConsumer) -> {

            List<ResultEntry> entries = new ArrayList<>();
            Set<Identifier> suggestionsSet = new HashSet<>();
            List<Pair<String,Identifier>> suggestions = new ArrayList<>();
            for (Registry<?> registry : Registries.REGISTRIES) {
                List<Identifier> results = new ArrayList<>();
                for (var tag : registry.streamTagsAndEntries().toList()) {
                    Identifier tagId = tag.getFirst().id();
                    if (tagId.equals(id)) {
                        for (var ahh : tag.getSecond().stream().toList()) {
                            var key = ahh.getKey();
                            if (key.isPresent())
                                results.add(key.get().getValue());
                        }
                    }
                    else  {
                        String ns = tagId.getNamespace();
                        String path = tagId.getPath();
                        if (path.startsWith(fText) || (!ns.equals("minecraft") && ns.startsWith(fText)) ) {
                            if (!suggestionsSet.contains(tagId)) {
                                suggestions.add(new Pair<>(getRegistryName(registry.getKey().getValue()), tagId));
                                suggestionsSet.add(tagId);
                            }
                        }

                    }
                }

                if (!results.isEmpty()) {
                    entries.add(new TagResultHeader(registry.getKey().getValue()));
                    for (var e : results)
                        entries.add(new TagResultEntry(e));
                }
            }
            for (var s : suggestions)
                entryConsumer.accept(new TagSuggestion(s));
            for (var e : entries)
                entryConsumer.accept(e);

        };
    }


    public static class Header implements LimelightExtension {
        public static final Identifier ID = Identifier.of("limelight_tags", "header");
        public static final Header INSTANCE = new Header();

        @Override
        public Identifier id() {
            return ID;
        }
    }
    public record TagResultHeader(Identifier result) implements InvokeResultEntry {

        @Override
        public LimelightExtension extension() {
            return Header.INSTANCE;
        }

        @Override
        public String entryId() {
            return UUID.randomUUID().toString();
        }

        @Override
        public Text text() {
            return Text.literal(getRegistryName(result));
        }

        @Override
        public void run() {}
    }

    public static String getRegistryName(Identifier result) {
        String path = WordUtils.capitalize(result.getPath().replace('_', ' '));
        if (result.getNamespace().equals("minecraft"))
            return path + " Tag";
        String ns = WordUtils.capitalize(result.getNamespace().replace('_', ' '));
        return ns + ": " + path + " Tag";
    }

    public record TagResultEntry(Identifier result) implements InvokeResultEntry {

        @Override
        public LimelightExtension extension() {
            return INSTANCE;
        }

        @Override
        public String entryId() {
            return UUID.randomUUID().toString();
        }

        @Override
        public Text text() {
            return Text.literal(result.getNamespace().equals("minecraft") ? result.getPath() : result().toString());
        }

        @Override
        public void run() {
            MinecraftClient.getInstance().setScreen(new ChatScreen(result.toString()));
        }
    }


    public static class TagSuggestion implements SetSearchTextEntry {
        public static final Identifier ID = Identifier.of("limelight_tags", "suggestion");

        public final String type;
        public final String text;
        public TagSuggestion(Pair<String,Identifier> result) {
            Identifier id = result.getRight();
            text = "#" + (id.getNamespace().equals("minecraft") ? id.getPath() : id.toString());
            type = result.getLeft();
        }

        @Override
        public LimelightExtension extension() {
            return new LimelightExtension() {
                @Override public Identifier id() { return ID; }
                @Override public Text name() { return Text.literal(type); }
            };
        }

        @Override
        public String entryId() {
            return UUID.randomUUID().toString();
        }

        @Override
        public Text text() {
            return Text.literal(text);
        }

        @Override
        public String newSearchText() {
            return text;
        }
    }
}
