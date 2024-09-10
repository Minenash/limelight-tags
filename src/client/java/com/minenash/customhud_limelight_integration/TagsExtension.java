package com.minenash.customhud_limelight_integration;

import io.wispforest.limelight.api.entry.*;
import io.wispforest.limelight.api.extension.LimelightExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TagsExtension implements LimelightExtension {
    public static final Identifier ID = Identifier.of("limelight_tags", "main");
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

        List<ResultEntry> equalEntries = new ArrayList<>();
        List<ResultEntry> suggestEntries = new ArrayList<>();

        for (Registry<?> registry : Registries.REGISTRIES) {
            for (var tag : registry.streamTagsAndEntries().toList()) {
                Identifier registryID = registry.getKey().getValue();
                Identifier tagId = tag.getFirst().id();

                if (tagId.equals(id))
                    equalEntries.add(new TagResultEntry(registryID, tagId, tag.getSecond()));
                else {
                    String ns = tagId.getNamespace();
                    String path = tagId.getPath();
                    if (path.startsWith(text) || (!ns.equals("minecraft") && ns.startsWith(text))
                    || (text.length() > 2 && path.contains(text))) {
                        suggestEntries.add(new TagResultEntry(registryID, tagId, tag.getSecond()));
                    }
                }
            }
        }

        return (ctx1, entryConsumer) -> {
            for (var entry : equalEntries)
                entryConsumer.accept(entry);
            for (var entry : suggestEntries)
                entryConsumer.accept(entry);
        };
    }

    public static List<ResultEntry> getTagEntries(RegistryEntryList.Named<?> list) {
        List<ResultEntry> entries = new ArrayList<>();
        for (var entry : list.stream().toList()) {
            var key = entry.getKey();
            if (key.isPresent())
                entries.add(new TagResultChildEntry(key.get().getValue()));
        }
        return entries;
    }

    public static String getRegistryName(Identifier result) {
        String path = WordUtils.capitalize(result.getPath().replace('_', ' '));
        if (result.getNamespace().equals("minecraft"))
            return path + " Tag";
        String ns = WordUtils.capitalize(result.getNamespace().replace('_', ' '));
        return ns + ": " + path + " Tag";
    }

    public static class TagResultEntry implements ExpandableResultEntry {
        public static final Identifier ID = Identifier.of("limelight_tags", "header");
        public final String type;
        public final String text;
        public final List<ResultEntry> entries;

        public TagResultEntry(Identifier registry, Identifier tag, RegistryEntryList.Named<?> list) {
            text = "#" + (tag.getNamespace().equals("minecraft") ? tag.getPath() : tag.toString());
            type = getRegistryName(registry);
            this.entries = getTagEntries(list);
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
            return text;
        }

        @Override
        public Text text() {
            return Text.literal(text);
        }

        @Override
        public List<ResultEntry> children() {
            return entries;
        }
    }

    public static class EntryExtension implements LimelightExtension {
        public static final EntryExtension INSTANCE = new EntryExtension();
        public static final Identifier ID = Identifier.of("limelight_tags", "entry");
        @Override public Identifier id() { return ID; }
    }
    public record TagResultChildEntry(Identifier result) implements InvokeResultEntry {

        @Override
        public LimelightExtension extension() {
            return EntryExtension.INSTANCE;
        }

        @Override
        public String entryId() {
            return result.toString();
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
}
