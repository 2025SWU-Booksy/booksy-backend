package com.booksy.global.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

  @Value("${external.openai.api-key}")
  private String apiKey;

  private final ObjectMapper objectMapper;

  private static final String API_URL = "https://api.openai.com/v1/chat/completions";

  RestTemplate restTemplate = new RestTemplate();

  public String askDifficulty(String title, String summary) {
    String prompt = String.format(
      """
        당신은 다양한 분야의 도서를 분석해 난이도를 평가하는 독서 큐레이터 AI입니다.

        다음 책의 난이도를 평가해주세요.

        📌 평가 기준:
        - 난이도는 반드시 ["초급", "중급", "고급"] 중 하나로 선택
        - 문체의 복잡성, 내용의 깊이, 주제의 난해함, 정보량, 구성 방식, 용어의 이해 난이도 등을 종합적으로 고려
        - 소설, 에세이, 자기계발, 인문, 과학, 예술 등 모든 분야의 도서를 공정하게 평가
        - 독자의 접근성(읽기 쉬움/이해 쉬움) 관점에서 판단

        📌 응답 형식:
        아래와 같은 **JSON 형식**으로 응답해 주세요.
        ※ 형식만 참고하고, 응답 내용은 자유롭게 작성하세요.
        ```json
        {"level": "중급", "reason": "내용이 다소 깊고 배경지식이 요구됨"}
        ```
        ---
                
        책 제목: %s
        책 소개: %s
        """, title, summary
    );

    ChatRequest request = new ChatRequest("gpt-3.5-turbo", List.of(
      new Message("system", "당신은 책 난이도를 평가하는 AI입니다."),
      new Message("user", prompt)
    ));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

    try {
      ChatResponse chatResponse = objectMapper.readValue(response.getBody(), ChatResponse.class);
      return chatResponse.getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      throw new RuntimeException("GPT 응답 파싱 실패", e);
    }
  }

  public String askRecommendation(int age, String gender) {
    String prompt = String.format(
      """
        %d세 %s(한국 거주자)에게 인기 있는 **한국 도서** 5권을 추천해줘.
            
        조건:
        - 반드시 실제로 출간된 한국 도서만 추천해줘 (즉, 한국 작가의 한국어로 쓰인 책)
        - 각 책은 제목(title), 저자(author)를 포함해줘
        - 응답은 다음 형식의 JSON 배열로 해줘:
          [{"title": "...", "author": "..."}, ...]
        """,
      age,
      gender.equalsIgnoreCase("F") ? "여성" : "남성"
    );

    ChatRequest request = new ChatRequest("gpt-3.5-turbo", List.of(
      new Message("system", "당신은 독서 큐레이터 AI입니다."),
      new Message("user", prompt)
    ));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

    try {
      return objectMapper.readTree(response.getBody())
        .get("choices")
        .get(0)
        .get("message")
        .get("content")
        .asText();
    } catch (Exception e) {
      throw new RuntimeException("GPT 도서 추천 응답 파싱 실패", e);
    }
  }

  @Getter
  @Setter
  @AllArgsConstructor
  static class Message {

    private String role;
    private String content;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  static class ChatRequest {

    private String model;
    private List<Message> messages;
  }

  @Getter
  @Setter
  static class ChatResponse {

    private List<Choice> choices;

    @Getter
    @Setter
    static class Choice {

      private Message message;
    }
  }
}
