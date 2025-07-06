package com.codeit.otboo.domain.dm.util;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DmKeyUtil {
	private DmKeyUtil() {

	}
	public static String makeDmKey(UUID senderId, UUID receiverId) {
		List<UUID> ids = List.of(senderId, receiverId);
		ids.sort(Comparator.naturalOrder());
		return ids.get(0) + "_" + ids.get(1);
	}
}
