package com.ihatecsv;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Iou implements ModInitializer {
	public static final String MOD_ID = "iou";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Item IOU_ITEM = Registry.register(
			Registries.ITEM,
			new Identifier(Iou.MOD_ID, "iou"),
			new IouItem(new Item.Settings().maxCount(64))
	);

	@Override
	public void onInitialize() {
		IouNetworking.registerServerReceiver();
		LOGGER.info("IOU mod initialised");
	}
}
