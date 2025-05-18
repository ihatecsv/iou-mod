package com.ihatecsv.iou;

import com.ihatecsv.iou.items.IOUItem;
import com.ihatecsv.iou.networking.IOUNetworkServer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOU implements ModInitializer {
	public static final String MOD_ID = "iou";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Item IOU_ITEM = Registry.register(
			Registries.ITEM,
			Identifier.of(IOU.MOD_ID, "iou"),
			new IOUItem(new Item.Settings().maxCount(64))
	);

	@Override
	public void onInitialize() {
		IOUComponents.init();
		IOUNetworkServer.registerServerReceiver();
		LOGGER.info("IOU mod initialised");
	}
}
