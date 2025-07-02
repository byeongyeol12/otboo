package com.codeit.otboo.domain.weather.component;

import com.codeit.otboo.domain.weather.entity.vo.LocationInfo;
import org.springframework.stereotype.Component;

@Component
public class LocationConverter {

    // 기상청 공식 가이드에서 제공하는 상수 값들
    private static final double RE = 6371.00877; // 지구 반경(km)
    private static final double GRID = 5.0;      // 격자 간격(km)
    private static final double SLAT1 = 30.0;    // 표준 위도 1
    private static final double SLAT2 = 60.0;    // 표준 위도 2
    private static final double OLON = 126.0;    // 기준점 경도
    private static final double OLAT = 38.0;     // 기준점 위도
    private static final int XO = 43;            // 기준점 X좌표
    private static final int YO = 136;           // 기준점 Y좌표

    /**
     * 위도와 경도를 기상청 격자 X, Y 좌표로 변환합니다.
     * @param latitude 위도
     * @param longitude 경도
     * @return 격자 X, Y 좌표가 포함된 LocationInfo 객체
     */
    public LocationInfo toGrid(double latitude, double longitude) {
        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + (latitude) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);

        double theta = longitude * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        double x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
        double y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        return new LocationInfo(latitude, longitude, (int) x, (int) y);
    }
}