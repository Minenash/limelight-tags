package com.minenash.customhud_limelight_integration;

import io.wispforest.limelight.api.LimelightEntrypoint;
import io.wispforest.limelight.api.extension.LimelightExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class Entrypoint implements LimelightEntrypoint {

	@Override
	public void registerExtensions(Consumer<LimelightExtension> extensionRegistry) {
		extensionRegistry.accept(CustomHudExtension.INSTANCE);
	}

}