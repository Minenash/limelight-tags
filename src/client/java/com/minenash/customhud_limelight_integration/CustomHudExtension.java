package com.minenash.customhud_limelight_integration;

import com.minenash.customhud.HudElements.interfaces.ExecuteElement;
import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.HudElements.interfaces.MultiElement;
import com.minenash.customhud.HudElements.list.ListProviderSet;
import com.minenash.customhud.VariableParser;
import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.conditionals.ExpressionParser;
import com.minenash.customhud.conditionals.Operation;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.errors.Errors;
import io.wispforest.limelight.api.entry.InvokeResultEntry;
import io.wispforest.limelight.api.entry.ResultEntryGatherer;
import io.wispforest.limelight.api.entry.ResultGatherContext;
import io.wispforest.limelight.api.extension.LimelightExtension;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomHudExtension implements LimelightExtension {
    public static final Identifier ID = Identifier.of("customhud_limelight_integration", "customhud");
    public static final CustomHudExtension INSTANCE = new CustomHudExtension();
    public static final String PROFILE_NAME = "␑Limelight";

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public @Nullable ResultEntryGatherer checkExclusiveGatherer(ResultGatherContext ctx) {
        String text = ctx.searchText();
        if (text.startsWith("$")) return expression(ctx, text.substring(1));
        if (text.startsWith("`")) return syntax(ctx, text.substring(1));
        return null;
    }

    public ResultEntryGatherer expression(ResultGatherContext ctx, String input) {
        Errors.clearErrors(PROFILE_NAME);
        Operation op = ExpressionParser.parseExpression(input, input, Profile.create(PROFILE_NAME), 0, new ComplexData.Enabled(), new ListProviderSet(), false);
        return (ctx1, entryConsumer) -> {
            String value = Double.toString(op.getValue());
            entryConsumer.accept(new CustomHudResultEntry(value, Text.literal(value), false));

            for (var e : Errors.getErrors(PROFILE_NAME)) {
                String str = "§4" + e.type().message + "§4" + e.context();
                entryConsumer.accept(new CustomHudResultEntry(str, Text.literal(str), true));
            }

        };
    }

    public ResultEntryGatherer syntax(ResultGatherContext ctx, String input) {
        Errors.clearErrors(PROFILE_NAME);
        List<HudElement> elements = VariableParser.addElements(input, Profile.create(PROFILE_NAME), 0, new ComplexData.Enabled(), false, new ListProviderSet());
        StringBuilder builder = new StringBuilder();
        for (HudElement element : elements) {
            if (element instanceof ExecuteElement ee)
                ee.run();
            else {
                String str = element.getString();
                if (str != null)
                    builder.append(str);
            }
        }

        String result = builder.toString();
        return (ctx1, entryConsumer) -> {
            entryConsumer.accept(new CustomHudResultEntry(result, Text.literal(result), false));
            for (var e : Errors.getErrors(PROFILE_NAME)) {
                String str = "§4" + e.type().message + "§4" + e.context();
                entryConsumer.accept(new CustomHudResultEntry(str, Text.literal(str), true));
            }

        };
    }


    public static class CustomHudErrorExtension implements LimelightExtension {
        public static final Identifier ID = Identifier.of("customhud_limelight_integration", "customhud.error");
        public static final CustomHudErrorExtension INSTANCE = new CustomHudErrorExtension();

        @Override
        public Identifier id() {
            return CustomHudErrorExtension.ID;
        }
    }

    public record CustomHudResultEntry(String id, Text result, boolean error) implements InvokeResultEntry {

        @Override
        public LimelightExtension extension() {
            return error ? CustomHudErrorExtension.INSTANCE : INSTANCE;
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
