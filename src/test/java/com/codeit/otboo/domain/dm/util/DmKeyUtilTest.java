package com.codeit.otboo.domain.dm.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DmKeyUtilTest {

	@Test
	@DisplayName("makeDmKey - 오름차순 정렬 성공")
	public void makeDmKey_success() {
		//given
		UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
		UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

		//when
		String result = DmKeyUtil.makeDmKey(id1, id2);

		//then
		assertEquals(id1.toString()+"_"+id2.toString(),result);
	}

	@Test
	@DisplayName("makeDmKey - 오름차순 정렬 성공(역순)")
	public void makeDmKey_reverse_success() {
		//given
		UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
		UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000001");

		//when
		String result = DmKeyUtil.makeDmKey(id1, id2);

		//then
		assertEquals(id2.toString()+"_"+id1.toString(),result);
	}

}
