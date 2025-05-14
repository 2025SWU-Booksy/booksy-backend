package com.booksy.domain.book.external;

import com.booksy.domain.book.external.dto.LibraryInfo;
import com.booksy.global.error.ErrorCode;
import com.booksy.global.error.exception.ApiException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 전국 도서관 통합자료검색(data4library) API를 호출하여 도서관 위치 정보 및 도서 소장/대출 가능 여부를 조회하는 외부 API 클라이언트
 */
@Component
@RequiredArgsConstructor
public class LibraryExternalClient {

  private final RestTemplate restTemplate = new RestTemplate();

  /**
   * data4library API 인증키
   */
  @Value("${external.library.api-key}")
  private String apiKey;

  /**
   * 위도/경도와 반경(km)을 기반으로 근처 도서관 목록을 조회한다. 내부적으로 data4library를 2페이지로 나눠 호출하고, 캐싱된 XML을 파싱해서 반경
   * 필터링한다.
   */
  public List<LibraryInfo> getNearbyLibraries(double latitude, double longitude, double radiusKm) {
    // 1페이지 + 2페이지 XML 호출 (캐싱됨)
    String xmlPage1 = getXmlByPage(1, 1000);
    String xmlPage2 = getXmlByPage(2, 1000);

    // 거리 필터링
    List<LibraryInfo> page1 = parseXmlResponse(xmlPage1, latitude, longitude, radiusKm);
    List<LibraryInfo> page2 = parseXmlResponse(xmlPage2, latitude, longitude, radiusKm);

    List<LibraryInfo> result = new ArrayList<>();
    result.addAll(page1);
    result.addAll(page2);

    return result;
  }

  /**
   * 각 페이지별 도서관 XML을 반환 (Caffeine 캐싱됨)
   */
  @Cacheable(value = "libraryXmlCache", key = "#pageNo")
  public String getXmlByPage(int pageNo, int pageSize) {
    URI uri = UriComponentsBuilder.fromHttpUrl("https://data4library.kr/api/libSrch")
        .queryParam("authKey", apiKey)
        .queryParam("pageNo", pageNo)
        .queryParam("pageSize", pageSize)
        .build()
        .toUri();

    return restTemplate.getForObject(uri, String.class);
  }

  /**
   * XML 응답을 파싱하고, 사용자 위치에서 반경 이내 도서관만 반환
   */
  private List<LibraryInfo> parseXmlResponse(String xmlResponse, double userLat, double userLng,
      double radiusKm) {
    List<LibraryInfo> libraries = new ArrayList<>();

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(xmlResponse)));

      NodeList libNodes = document.getElementsByTagName("lib");

      for (int i = 0; i < libNodes.getLength(); i++) {
        Element libElement = (Element) libNodes.item(i);

        String libCode = getTextContent(libElement, "libCode");
        String libName = getTextContent(libElement, "libName");
        String latStr = getTextContent(libElement, "latitude");
        String lngStr = getTextContent(libElement, "longitude");

        if (libCode != null && libName != null && latStr != null && lngStr != null) {
          double libLat = Double.parseDouble(latStr);
          double libLng = Double.parseDouble(lngStr);
          double distance = calculateDistance(userLat, userLng, libLat, libLng);

          if (distance <= radiusKm) {
            libraries.add(new LibraryInfo(libCode, libName, libLat, libLng));
          }
        }
      }
    } catch (Exception e) {
      throw new ApiException(ErrorCode.LIBRARY_API_UNAVAILABLE);
    }

    return libraries;
  }

  /**
   * 두 좌표 간 거리 계산 (단위: km)
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  /**
   * XML 태그에서 텍스트 값 추출
   */
  private String getTextContent(Element parent, String tagName) {
    NodeList nodeList = parent.getElementsByTagName(tagName);
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getTextContent().trim();
    }
    return null;
  }
}