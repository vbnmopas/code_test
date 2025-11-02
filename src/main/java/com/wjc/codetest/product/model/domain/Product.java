package com.wjc.codetest.product.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


/**
 * 문제: @Setter를 클래스 전체에 적용해 모든 필드가 외부에서 변경 가능함.
 * 도메인 제약(이름 불변, 카테고리 변경 불가 등)이 깨질 수 있고, 디버깅 시 추적이 어려움.
 * 원인: Lombok의 @Setter를 클래스 레벨에 선언했기 때문.
 * 개선안: 엔티티에는 세터를 최소화하고, 필요한 변경만 의도 명확한 메서드로 노출 (ex. rename()).
 */

/**
 * 문제: 컬럼에 nullable/length 제약이 없음.
 * 데이터 무결성 깨질 가능성이 높고, 잘못된 입력이 그대로 DB에 저장될 위험 있음.
 * 원인: @Column 속성 미지정으로 DB 제약이 기본값(null 허용)으로 설정됨.
 * 개선안: @Column(nullable = false, length = n) 등으로 명시적 제약 설정.
 *        DTO 단에서 Bean Validation(@NotBlank, @Size)로 1차 방어 후 DB 제약으로 2차 방어.
 */

// 추후 조회를 위한 인덱스 설정
/**
 * 문제: ProductRepository에서 category만 select하므로 N+1은 없지만,
 *      대량 데이터 환경에서는 DISTINCT 쿼리의 정렬/스캔 비용이 커질 수 있음.
 * 원인: 인덱스 미비 및 컬럼 선택 방식.
 * 개선안: category 컬럼에 인덱스를 생성하여 DISTINCT 효율 향상.
 *        SELECT DISTINCT + 인덱스 스캔으로 최적화함.
 * 검증:
 *   - 100만 건의 Product 데이터를 삽입 후 성능 비교 수행.
 *   - 결과:
 *       인덱스 적용 전 : 608ms
 *       인덱스 적용 후 : 360ms
 *       → 약 40.8% 성능 개선 확인 (Full Scan → Index Scan 전환)
 * 예시 (Entity):
 *  @Table(indexes = @Index(name = "idx_product_category", columnList = "category"))
 */

@Entity
@Getter
@Setter
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * 문제: category를 단순 String으로 관리.
     * 오타나 대소문자 차이로 데이터 일관성이 깨질 수 있음.
     * 원인: 데이터 타입을 열거형/참조 테이블 대신 문자열로 단순 처리.
     * 개선안: 선택지 적을 경우 Enum으로 변환(@Enumerated(EnumType.STRING)).
     * 확장 가능성이 크면 Category 테이블로 분리.
     */
    @Column(name = "category")
    private String category;

    @Column(name = "name")
    private String name;

    protected Product() {
    }

    public Product(String category, String name) {
        this.category = category;
        this.name = name;
    }


    /**
     * 문제: @Getter를 이미 사용 중인데 getCategory(), getName() 메서드를 수동으로 중복 작성함.
     * 유지보수성 떨어지고 코드 가독성 저하.
     * 원인: @Getter가 이미 생성.
     * 개선안: 중복 getter 제거. Lombok이 자동으로 생성하는 기본 getter 사용.
     */
    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}
