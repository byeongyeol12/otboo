package com.codeit.otboo.domain.dm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DmKeyUtil {
	private DmKeyUtil() {

	}
	public static String makeDmKey(UUID senderId, UUID receiverId) {
		List<String> ids = new ArrayList<>(List.of(senderId.toString(), receiverId.toString())); // 수정 가능 리스트로 변환
		ids.sort(String::compareTo);
		return ids.get(0) + "_" + ids.get(1);
	}
}
