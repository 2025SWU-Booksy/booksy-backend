package com.booksy.domain.book.mapper;

import com.booksy.domain.book.dto.BookResponseDto;
import com.booksy.domain.book.entity.Book;
import com.booksy.domain.book.external.dto.AladinItemDto;
import com.booksy.domain.book.external.dto.SubInfoDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Book 엔티티와 BookResponseDto 간의 변환을 담당하는 매퍼 클래스
 */
@Component
public class BookMapper {

  /**
   * Book 엔티티 → BookResponseDto 변환
   *
   * @param book 변환할 Book 객체
   * @return BookResponseDto
   */
  public BookResponseDto toDto(Book book) {
    if (book == null) {
      return null;
    }

    return BookResponseDto.builder()
        .isbn(book.getIsbn())
        .title(book.getTitle())
        .author(book.getAuthor())
        .publisher(book.getPublisher())
        .publishedDate(book.getPublishedDate())
        .totalPage(book.getTotalPage())
        .imageUrl(book.getImageUrl())
        .description(book.getFullDescription())
        .build();
  }

  /**
   * Book 리스트 → BookResponseDto 리스트 변환
   *
   * @param books Book 객체 리스트
   * @return BookResponseDto 리스트
   */
  public List<BookResponseDto> toDtoList(List<Book> books) {
    return books.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  /**
   * BookResponseDto → Book 엔티티 변환
   */
  public Book toEntity(BookResponseDto dto) {
    if (dto == null) {
      return null;
    }

    return Book.builder()
        .isbn(dto.getIsbn())
        .title(dto.getTitle())
        .author(dto.getAuthor())
        .publisher(dto.getPublisher())
        .publishedDate(dto.getPublishedDate())
        .totalPage(dto.getTotalPage())
        .imageUrl(dto.getImageUrl())
        .fullDescription(dto.getDescription())
        .build();
  }

  /**
   * AladinItemDto → BookResponseDto 변환
   */
  public BookResponseDto toDto(AladinItemDto item) {
    if (item == null) {
      return null;
    }

    SubInfoDto sub = item.getSubInfo();
    String description = null;

    // ❗ 임시값 - 수정예정
    if (sub != null) {
      if (sub.getFulldescription() != null && !sub.getFulldescription().isBlank()) {
        description = sub.getFulldescription();
      } else if (sub.getFulldescription2() != null && !sub.getFulldescription2().isBlank()) {
        description = sub.getFulldescription2(); // fallback
      } else {
        description =
            "과도한 이자를 물며 돈을 빌리거나, 반복적으로 약속에 늦고, 다이어트를 결심해도 며칠을 못 버티는 사람들. 이처럼 경솔한 행동을 하는 사람들의 공통점은"
                + " 무엇일까? 하버드대 경제학과 교수 센딜 멀레이너선과 프린스턴대 교수 엘다 샤퍼는 『결핍은 우리를 어떻게 변화시키는가』에서 흥미로운 답을 "
                + "제시한다. 이들의 비합리적인 행동은 개인의 지능이나 자제력 부족이 아닌, ‘결핍’에서 비롯된다는 것이다.\n"
                + "\n"
                + "물론 결핍이 반드시 부정적인 것만은 아니다. 오히려 결핍은 우리의 잠재력을 끌어내는 원동력이 되기도 한다. 시간이 부족할 때 집중력이 "
                + "높아지거나, 쓸 수 있는 자원이 한정되어 있을 때 더 창의적인 해결책을 찾아낼 수 있는 이유도 바로 이 때문이다. 하지만 이 집중이 지나치면"
                + " 다른 중요한 것을 놓칠 수도 있다.\n"
                + "\n"
                + "가난한 사람이 지출을 줄이기 위해 꼭 필요한 보험을 해지하고, 바쁜 사람이 약속 시간에 맞추려고 무단횡단을 하는 것처럼 말이다.『결핍은 "
                + "우리를 어떻게 변화시키는가』는 이와 같이 결핍이 우리의 인지 능력에 영향을 미치는 여러 사례와 방대한 연구 결과를 살펴보며 결핍이 우리의 "
                + "행동과 의사 결정에 미치는 영향을 상세히 분석한다.";
      }
    }

    return BookResponseDto.builder()
        .isbn(item.getIsbn13())
        .title(item.getTitle())
        .author(item.getAuthor())
        .publisher(item.getPublisher())
        .publishedDate(parseDate(item.getPubDate()))
        .totalPage(item.getSubInfo() != null ? item.getSubInfo().getItemPage() : 0)
        .imageUrl(item.getCover())
        .description(description)
        .build();
  }

  /**
   * AladinItemDto 리스트 → BookResponseDto 리스트 변환
   */
  public List<BookResponseDto> toDtoListFromAladin(List<AladinItemDto> items) {
    if (items == null) {
      return Collections.emptyList();
    }
    return items.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  /**
   * 문자열 형태의 pubDate("yyyy-MM-dd") → LocalDate 변환
   */
  private LocalDate parseDate(String pubDate) {
    try {
      return LocalDate.parse(pubDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } catch (Exception e) {
      return null;
    }
  }
}
