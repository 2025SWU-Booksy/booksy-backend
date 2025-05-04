package com.booksy.domain.readinglog.service;

import com.booksy.domain.readinglog.dto.DictionaryResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class DictionaryService {

  @Value("${external.dictionary.api-key}")
  private String apiKey;

  @Value("${external.dictionary.base-url}")
  private String baseUrl;

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 표준국어대사전 API 호출 후 정의 추출
   */
  public DictionaryResponseDto searchWord(String keyword) {
    try {
      String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
          .queryParam("key", apiKey)
          .queryParam("q", keyword)
          .queryParam("req_type", "json")
          .build()
          .toUriString();

      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(List.of(MediaType.APPLICATION_JSON));
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          String.class
      );

      JsonNode root = objectMapper.readTree(response.getBody());
      JsonNode items = root.path("channel").path("item");

      List<String> definitions = new ArrayList<>();

      if (items.isArray() && items.size() > 0) {
        for (JsonNode item : items) {
          JsonNode senseNode = item.path("sense");

          if (senseNode.isArray() && senseNode.size() > 0) {
            for (JsonNode sense : senseNode) {
              String def = sense.path("definition").asText();
              if (!def.isBlank()) {
                definitions.add(def);
              }
            }
          } else if (senseNode.isObject()) {
            String def = senseNode.path("definition").asText();
            if (!def.isBlank()) {
              definitions.add(def);
            }
          }
        }
      }

      if (definitions.isEmpty()) {
        definitions.add("해당 단어의 정의를 찾을 수 없습니다.");
      }

      return new DictionaryResponseDto(keyword, definitions);

    } catch (Exception e) {
      e.printStackTrace();
      return new DictionaryResponseDto(keyword, List.of("사전 API 호출에 실패했습니다."));
    }
  }
}