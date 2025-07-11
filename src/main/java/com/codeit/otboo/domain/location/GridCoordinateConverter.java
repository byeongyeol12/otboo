package com.codeit.otboo.domain.location;

import org.springframework.stereotype.Component;

@Component
public class GridCoordinateConverter {

	private static final double RE = 6371.00877; // 지구 반경 (km)
	private static final double GRID = 5.0; // 격자 간격 (km)
	private static final double SLAT1 = 30.0; // 투영 위도1
	private static final double SLAT2 = 60.0; // 투영 위도2
	private static final double OLON = 126.0; // 기준 경도
	private static final double OLAT = 38.0; // 기준 위도
	private static final double XO = 43; // 기준 x 좌표
	private static final double YO = 136; // 기준 y 좌표

	private final double DEGRAD = Math.PI / 180.0;
	private final double re;
	private final double slat1;
	private final double slat2;
	private final double olon;
	private final double olat;
	private final double sn;
	private final double sf;
	private final double ro;

	public GridCoordinateConverter() {
		this.re = RE / GRID;
		this.slat1 = SLAT1 * DEGRAD;
		this.slat2 = SLAT2 * DEGRAD;
		this.olon = OLON * DEGRAD;
		this.olat = OLAT * DEGRAD;

		this.sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) /
			Math.log(Math.tan(Math.PI / 4 + slat2 / 2) / Math.tan(Math.PI / 4 + slat1 / 2));
		this.sf = Math.pow(Math.tan(Math.PI / 4 + slat1 / 2), sn) * Math.cos(slat1) / sn;
		this.ro = re * sf / Math.pow(Math.tan(Math.PI / 4 + olat / 2), sn);
	}

	public GridCoordinate toGrid(double lat, double lon) {
		double ra = re * sf / Math.pow(Math.tan(Math.PI / 4 + (lat * DEGRAD) / 2), sn);
		double theta = lon * DEGRAD - olon;

		if (theta > Math.PI)
			theta -= 2.0 * Math.PI;
		if (theta < -Math.PI)
			theta += 2.0 * Math.PI;
		theta *= sn;

		int x = (int)(ra * Math.sin(theta) + XO + 0.5);
		int y = (int)(ro - ra * Math.cos(theta) + YO + 0.5);

		return new GridCoordinate(x, y);
	}
}

