package com.minenash.customhud_limelight_integration;

import io.wispforest.limelight.api.entry.InvokeResultEntry;
import io.wispforest.limelight.api.entry.ResultEntryGatherer;
import io.wispforest.limelight.api.entry.ResultGatherContext;
import io.wispforest.limelight.api.extension.LimelightExtension;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomHudExtension implements LimelightExtension {
    public static final Identifier ID = Identifier.of("customhud_limelight_integration", "customhud");
    public static final CustomHudExtension INSTANCE = new CustomHudExtension();

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public @Nullable ResultEntryGatherer checkExclusiveGatherer(ResultGatherContext ctx) {
        String text = ctx.searchText();
        if (!text.startsWith("#")) return null;

        Identifier id = Identifier.tryParse(text.substring(1));
        if (id == null) return null;

        List<String> results = new ArrayList<>();
        for (Registry<?> registry : Registries.REGISTRIES) {
            for (var tag : registry.streamTagsAndEntries().toList()) {
                if (tag.getFirst().id().equals(id)) {
                    for (var ahh : tag.getSecond().stream().toList()) {
                        ahh.getIdAsString();
                        results.add(ahh.getIdAsString());
                    }
                }
            }
        }
        return (ctx1, entryConsumer) -> {
            for (var e : results)
                entryConsumer.accept(new CustomHudResultEntry(e, Text.literal(e)));
        };
    }

    public record CustomHudResultEntry(String id, Text result) implements InvokeResultEntry {

        @Override
        public LimelightExtension extension() {
            return INSTANCE;
        }

        @Override
        public String entryId() {
            return id;
        }

        @Override
        public Text text() {
            return result;
        }

        @Override
        public void run() {}
    }
}
