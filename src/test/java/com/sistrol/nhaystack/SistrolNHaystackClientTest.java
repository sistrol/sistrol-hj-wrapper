package com.sistrol.nhaystack;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SistrolNHaystackClientTest {
	private static final String NIAGARA_URI = "http://host:port/";
	private static final String NIAGARA_USERNAME = "user";
	private static final String NIAGARA_PWD = "password";
	private SistrolNHaystackClient client;

	@org.testng.annotations.BeforeMethod
	public void setUp() {
		this.client = new SistrolNHaystackClient(NIAGARA_URI, NIAGARA_USERNAME, NIAGARA_PWD);
	}

	@org.testng.annotations.Test
	public void testRead() {
		String filter = "his and kind==\"Number\" and not cur";
		try {
			JSONObject response = client.read(filter);
			if (response!=null) {
				System.out.println(response.toJSONString());
			}
			assertNotNull(response);
		} catch (Exception e) {
			fail(e.getMessage(), e);
		}
	}

	@org.testng.annotations.Test
	public void testDateFormatting() {
		DateTime now = DateTime.now();
		String year = now.year().getAsString();
		String month = now.monthOfYear().getAsString();
		String day = now.dayOfMonth().getAsString();

		String stringDate = String.format("%s%s%s", year, StringUtils.leftPad(month, 2, "0"), StringUtils.leftPad(day, 2, "0"));
		DateTime calculatedDate = DateTime.parse(stringDate, DateTimeFormat.forPattern("yyyyMMdd"));

		assertEquals(now.withMillisOfDay(0).toDate(), calculatedDate.withMillisOfDay(0).toDate());
	}

	@Test
	public void testHisRead() {
		String axHistoryId = "H.Alvia_J1.AR_GC03_UPS_EnerActTot";
		try {
			JSONObject response = client.hisRead(axHistoryId, "2022-08-01");
			if (response!=null) {
				System.out.println(response.toJSONString());
			}
			assertNotNull(response);
		} catch (Exception e) {
			fail(e.getMessage(), e);
		}
	}
}